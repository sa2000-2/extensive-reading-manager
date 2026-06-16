package com.example.extensivereading.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.security.access.AccessDeniedException;

import com.example.extensivereading.dto.FavoriteForm;
import com.example.extensivereading.dto.RecommendResponse;
import com.example.extensivereading.entity.Favorite;
import com.example.extensivereading.repository.FavoriteRepository;

/**
 * RecommendServiceのテストクラス。
 * AIおすすめ本取得処理およびお気に入りの保存・取得・削除処理をテストする。
 */
@ExtendWith(MockitoExtension.class)
public class RecommendServiceTest {
	@Mock
	private ChatClient chatClient;
	
	@Mock
	private ChatClient.Builder chatClientBuilder;
	
	@Mock
    private FavoriteRepository favoriteRepository;
	
	@Mock
	private ChatClient.ChatClientRequestSpec requestSpec;
	
	@Mock
	private ChatClient.CallResponseSpec responseSpec;
	
	private RecommendService recommendService;
	
	/**
	 * コンストラクタ内部で builder.build() が実行されるため、
	 * 事前にbuild()のモック挙動を定義した上で手動でインスタンスを生成する。
	 */
	@BeforeEach
	void setUp() {
		when(chatClientBuilder.build()).thenReturn(chatClient);
		recommendService = new RecommendService(chatClientBuilder, favoriteRepository);
	}

	/**
	 * executeRecommendメソッドにおいて本の種類が「Graded Readers」のとき、
	 * AI通信が正常に行われ、返ってきたJSONがDTOリストに変換されることを確認するテスト。
	 */
	@Test
	void executeRecommend_GradedReaders_ReturnRecommendResponseList() {
		String level = "3";
		String type = "Graded Readers";
		String genre = "Mystery";

		String mockAiJson = """
				[
				  {"title": "Book A", "author": "Author A", "publisher": "AAA", "summary": "Summary A"},
				  {"title": "Book B", "author": "Author B", "publisher": "BBB", "summary": "Summary B"},
				  {"title": "Book C", "author": "Author C", "publisher": "CCC", "summary": "Summary C"}
				]
				""";

		when(chatClient.prompt()).thenReturn(requestSpec);
		when(requestSpec.user(anyString())).thenReturn(requestSpec);
		when(requestSpec.call()).thenReturn(responseSpec);
		when(responseSpec.content()).thenReturn(mockAiJson);

		List<RecommendResponse> responses = recommendService.executeRecommend(level, type, genre);

		assertNotNull(responses);
		assertEquals(3, responses.size());
		assertEquals("Book A", responses.get(0).getTitle());
		assertEquals("AAA", responses.get(0).getPublisher());
	}

	/**
	 * executeRecommendメソッドにおいて本の種類が「一般書」のとき、
	 * 正常にAI通信が行われ、結果が取得されることを確認するテスト。
	 */
	@Test
	void executeRecommend_GeneralBooks_ReturnRecommendResponseList() {
		// Arrange
		String level = "5";
		String type = "General Books";
		String genre = "Fantasy";

		String mockAiJson = """
				[
				  {"title": "Book D", "author": "Author D", "publisher": "DDD", "summary": "Summary D"}
				]
				""";

		when(chatClient.prompt()).thenReturn(requestSpec);
		when(requestSpec.user(anyString())).thenReturn(requestSpec);
		when(requestSpec.call()).thenReturn(responseSpec);
		when(responseSpec.content()).thenReturn(mockAiJson);

		// Act
		List<RecommendResponse> responses = recommendService.executeRecommend(level, type, genre);

		// Assert
		assertNotNull(responses);
		assertEquals(1, responses.size());
		assertEquals("Book D", responses.get(0).getTitle());
	}

	/**
	 * saveFavoriteメソッドにおいておすすめ本のお気に入り登録データが
	 * データベースに保存されることを確認するテスト。
	 */
	@Test
	void saveFavorite_ValidForm_SaveSuccessfully() {
		String userId = "user123";
		FavoriteForm form = new FavoriteForm();
		form.setTitle("タイトル");
		form.setAuthor("著者");
		form.setPublisher("出版社");
		form.setSummary("あらすじ");

		recommendService.saveFavorite(userId, form);

		ArgumentCaptor<Favorite> favoriteCaptor = ArgumentCaptor.forClass(Favorite.class);
		verify(favoriteRepository).save(favoriteCaptor.capture());

		Favorite savedFavorite = favoriteCaptor.getValue();
		assertEquals(userId, savedFavorite.getUserId());
		assertEquals("タイトル", savedFavorite.getBookTitle());
		assertNotNull(savedFavorite.getSavedDate());
	}

	/**
	 * saveFavoriteメソッドにおいて入力されたフォームの各項目に前後の空白があったとき、
	 * すべて削除され保存されることを確認するテスト。
	 */
	@Test
	void saveFavorite_FormWithSpaces_SaveTrimmedValues() {
		String userId = "user123";
		FavoriteForm form = new FavoriteForm();
		form.setTitle("   タイトル   ");
		form.setAuthor("   著者   ");
		form.setPublisher("  出版社   ");
		form.setSummary(" あらすじ   ");

		recommendService.saveFavorite(userId, form);

		ArgumentCaptor<Favorite> favoriteCaptor = ArgumentCaptor.forClass(Favorite.class);
		verify(favoriteRepository).save(favoriteCaptor.capture());

		Favorite savedFavorite = favoriteCaptor.getValue();
		assertEquals("タイトル", savedFavorite.getBookTitle());
		assertEquals("著者", savedFavorite.getAuthor());
		assertEquals("出版社", savedFavorite.getPublisher());
		assertEquals("あらすじ", savedFavorite.getSummary());
	}

	/**
	 * getFavoritesメソッドにおいて指定されたユーザーIDのお気に入りリストが
	 * 登録日順で取得されることを確認するテスト。
	 */
	@Test
	void getFavorites_UserExists_ReturnFavoriteList() {
		String userId = "user123";
		List<Favorite> mockList = new ArrayList<>();
		Favorite f = new Favorite();
		f.setUserId(userId);
		f.setBookTitle("お気に入り本");
		mockList.add(f);

		when(favoriteRepository.findByUserIdOrderBySavedDateDesc(userId)).thenReturn(mockList);

		List<Favorite> actualList = recommendService.getFavorites(userId);

		assertNotNull(actualList);
		assertEquals(1, actualList.size());
		assertEquals("お気に入り本", actualList.get(0).getBookTitle());
	}

	/**
	 * deleteFavoriteメソッドにおいてユーザーIDとお気に入りIDを渡したとき、
	 * データが削除されることを確認するテスト。
	 */
	@Test
	void deleteFavorite_ValidUserAndId_DeleteSuccessfully() {
		// Arrange
		String userId = "user123";
		Integer favoriteId = 1;

		Favorite existingFavorite = new Favorite();
		existingFavorite.setId(favoriteId);
		existingFavorite.setUserId(userId);

		when(favoriteRepository.findById(favoriteId)).thenReturn(Optional.of(existingFavorite));

		// Act
		recommendService.deleteFavorite(userId, favoriteId);

		// Assert & Verify
		verify(favoriteRepository).delete(existingFavorite);
	}

	/**
	 * deleteFavoriteメソッドにおいてお気に入りデータがデータベースに存在しない場合、
	 * IllegalArgumentExceptionが発生することを確認するテスト。
	 */
	@Test
	void deleteFavorite_FavoriteNotExists_ThrowIllegalArgumentException() {
		Integer favoriteId = 1;
		when(favoriteRepository.findById(favoriteId)).thenReturn(Optional.empty());

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
			recommendService.deleteFavorite("user123", favoriteId);
		});

		assertEquals("指定されたお気に入りが見つかりません。", exception.getMessage());
		verify(favoriteRepository, never()).delete(any(Favorite.class));
	}

	/**
	 * deleteFavoriteメソッドにおいてデータベースのユーザーIDとログインユーザーIDが異なる場合、
	 * セキュリティのためにAccessDeniedExceptionが発生することを確認するテスト。
	 */
	@Test
	void deleteFavorite_NotSameUserId_ThrowAccessDeniedException() {
		String myUserId = "user123";
		String hackerUserId = "hacker_user";
		Integer favoriteId = 1;

		Favorite victimFavorite = new Favorite();
		victimFavorite.setId(favoriteId);
		victimFavorite.setUserId(myUserId);

		when(favoriteRepository.findById(favoriteId)).thenReturn(Optional.of(victimFavorite));

		AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
			recommendService.deleteFavorite(hackerUserId, favoriteId);
		});

		assertEquals("他のユーザーのデータは削除できません。", exception.getMessage());
		verify(favoriteRepository, never()).delete(any(Favorite.class));
	}

}
