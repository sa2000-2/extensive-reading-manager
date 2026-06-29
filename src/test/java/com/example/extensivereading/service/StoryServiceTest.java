package com.example.extensivereading.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

import com.example.extensivereading.dto.StoryResponse;
import com.example.extensivereading.entity.BookRecord;
import com.example.extensivereading.entity.Story;
import com.example.extensivereading.repository.BookRecordRepository;
import com.example.extensivereading.repository.StoryRepository;

/**
 * StoryServiceのテストクラス。
 * AIストーリー生成処理およびストーリーの保存・取得・削除処理をテストする。
 */
@ExtendWith(MockitoExtension.class)
public class StoryServiceTest {

	@Mock
	private ChatClient chatClient;

	@Mock
	private ChatClient.Builder chatClientBuilder;

	@Mock
	private StoryRepository storyRepository;

	@Mock
	private BookRecordRepository bookRecordRepository;

	@Mock
	private ChatClient.ChatClientRequestSpec requestSpec;

	@Mock
	private ChatClient.CallResponseSpec responseSpec;

	private StoryService storyService;

	/**
	 * コンストラクタ内部で builder.build() が実行されるため、
	 * 事前にbuild()のモック挙動を定義した上で手動でインスタンスを生成する。
	 */
	@BeforeEach
	void setUp() {
		when(chatClientBuilder.build()).thenReturn(chatClient);
		storyService = new StoryService(chatClientBuilder, storyRepository, bookRecordRepository);
	}

	/**
	 * isInvalidメソッドにおいてジャンルが前後の空白や-を含む場合、
	 * 正常なジャンルとして判定されfalseを返すことを確認するテスト。
	 */
	@Test
	void isInvalid_ValidGenres_ReturnFalse() {
		assertFalse(StoryService.AllowedGenre.isInvalid("Mystery"));
		assertFalse(StoryService.AllowedGenre.isInvalid("Fantasy"));
		assertFalse(StoryService.AllowedGenre.isInvalid("  sci-fi  "));
	}

	/**
	 * isInvalidメソッドにおいて存在しないジャンルやnull、空白のみが指定された場合、
	 * trueを返すことを確認するテスト。
	 */
	@Test
	void isInvalid_InvalidOrEmptyGenres_ReturnTrue() {
		assertTrue(StoryService.AllowedGenre.isInvalid("Horror"));
		assertTrue(StoryService.AllowedGenre.isInvalid(null));
		assertTrue(StoryService.AllowedGenre.isInvalid("   "));
	}

	/**
	 * executeGenerateメソッドにおいて正常にAI通信が行われた場合、
	 * 返ってきたJSONがDTOに変換されることを確認するテスト。
	 */
	@Test
	void executeGenerate_AllValidInputs_ReturnParsedStoryResponse() {
		String mockJson = """
				{"title": "AAA", "contents": "AAAA", "wordCount": "100", "japanese": "AAAAA"}
				""";
		when(chatClient.prompt()).thenReturn(requestSpec);
		when(requestSpec.user(anyString())).thenReturn(requestSpec);
		when(requestSpec.call()).thenReturn(responseSpec);
		when(responseSpec.content()).thenReturn(mockJson);

		StoryResponse result = storyService.executeGenerate("3", "100", "Mystery");

		assertNotNull(result);
		assertEquals("AAA", result.getTitle());
		assertEquals("AAAA", result.getContents());
		assertEquals("100", result.getWordCount());
		assertEquals("AAAAA", result.getJapanese());
	}

	/**
	 * executeGenerateメソッドにおいて不正なレベルが入力された場合
	 * IllegalArgumentExceptionを出すかテスト
	 */
	@Test
	void executeGenerate_InvalidLevel_ThrowIllegalArgumentException() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
			storyService.executeGenerate("10", "100", "Mystery");
		});
		assertEquals("不正なレベルが選択されました。", exception.getMessage());
	}

	/**
	 * executeGenerateメソッドにおいて不正な語数が入力された場合
	 * IllegalArgumentExceptionを出すかテスト
	 */
	@Test
	void executeGenerate_InvalidWordCount_ThrowIllegalArgumentException() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
			storyService.executeGenerate("3", "150", "Mystery");
		});
		assertEquals("不正な語数が選択されました。", exception.getMessage());
	}

	/**
	 * executeGenerateメソッドにおいて不正なジャンルが入力された場合
	 * IllegalArgumentExceptionを出すかテスト
	 */
	@Test
	void executeGenerate_InvalidGenre_ThrowIllegalArgumentException() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
			storyService.executeGenerate("3", "100", "Comic");
		});
		assertEquals("不正なジャンルが選択されました。", exception.getMessage());
	}

	/**
	 * executeGenerateメソッドにおいてAIから規定文字数を超えるデータが返却された場合、
	 * 指定文字数で綺麗に切り詰められ、末尾に「…」が付与されることを確認するテスト。
	 */
	@Test
	void executeGenerate_FieldsExceedMaxLength_TruncateWithEllipsis() {
		String ultraLongTitle = "T".repeat(105);
		String mockJson = """
				{"title": "%s", "contents": "Text", "wordCount": "100", "japanese": "訳"}
				""".formatted(ultraLongTitle);

		when(chatClient.prompt()).thenReturn(requestSpec);
		when(requestSpec.user(anyString())).thenReturn(requestSpec);
		when(requestSpec.call()).thenReturn(responseSpec);
		when(responseSpec.content()).thenReturn(mockJson);

		StoryResponse result = storyService.executeGenerate("3", "100", "Mystery");

		assertEquals(101, result.getTitle().length());
		assertTrue(result.getTitle().endsWith("…"));
	}

	/**
	 * registerReadメソッドにおいて読書記録が正しく生成され、
	 * データベースへ保存されることを確認するテスト。
	 */
	@Test
	void registerRead_ValidInputs_SaveBookRecordCorrectly() {
		String userId = "user1";
		StoryResponse response = new StoryResponse();
		response.setTitle("  Test Book  ");

		String messyContents = "  AA, BBB.  \n  CCCC    DDDD!  ";
		response.setContents(messyContents);
		response.setWordCount("20"); 

		storyService.registerRead(userId, response);

		ArgumentCaptor<BookRecord> captor = ArgumentCaptor.forClass(BookRecord.class);
		verify(bookRecordRepository).save(captor.capture());

		BookRecord saved = captor.getValue();
		assertEquals(userId, saved.getUserId());
		assertEquals("Test Book", saved.getBookTitle());
		
		assertEquals(4, saved.getWordCount()); 
		assertNotNull(saved.getReadDate());
	}

	/**
	 * saveStoryメソッドにおいてストーリー情報がStoryエンティティへ正しく設定され、
	 * データベースへ保存されることを確認するテスト。
	 */
	@Test
	void saveStory_ValidInputs_SaveStoryEntityCorrectly() {
		String userId = "user1";
		StoryResponse response = new StoryResponse();
		response.setTitle(" Title ");
		response.setContents(" Contents ");
		response.setWordCount(" 300 ");
		response.setJapanese(" 日本語 ");

		storyService.saveStory(userId, response);

		ArgumentCaptor<Story> captor = ArgumentCaptor.forClass(Story.class);
		verify(storyRepository).save(captor.capture());

		Story saved = captor.getValue();
		assertEquals(userId, saved.getUserId());
		assertEquals("Title", saved.getTitle());
		assertEquals("Contents", saved.getContents());
		assertEquals("300", saved.getWordCount());
		assertEquals("日本語", saved.getJapanese());
		assertNotNull(saved.getSavedDate());
	}

	/**
	 * getStoryメソッドにおいて指定したユーザーIDに紐づくストーリー一覧が
	 * 保存日時の降順で取得されることを確認するテスト。
	 */
	@Test
	void getStory_UserExists_ReturnOrderedList() {
		String userId = "user1";
		List<Story> mockStories = List.of(new Story(), new Story());
		when(storyRepository.findByUserIdOrderBySavedDateDesc(userId)).thenReturn(mockStories);

		List<Story> result = storyService.getStory(userId);

		assertEquals(2, result.size());
		verify(storyRepository).findByUserIdOrderBySavedDateDesc(userId);
	}

	/**
	 * getStoryByIdメソッドにおいてユーザーIDとストーリー情報のユーザーIDが一致する場合、
	 * 保存したストーリーが取得できることを確認するテスト。
	 */
	@Test
	void getStoryById_ValidUserAndId_ReturnEntity() {
		String userId = "user1";
		Integer storyId = 99;
		Story mockStory = new Story();
		mockStory.setStoryId(storyId);
		mockStory.setUserId(userId);

		when(storyRepository.findById(storyId)).thenReturn(Optional.of(mockStory));

		Story result = storyService.getStoryById(userId, storyId);

		assertNotNull(result);
		assertEquals(storyId, result.getStoryId());
	}

	/**
	 * getStoryByIdメソッドにおいて指定したストーリーIDが存在しない場合、
	 * IllegalArgumentExceptionが発生することを確認するテスト。
	 */
	@Test
	void getStoryById_StoryNotFound_ThrowIllegalArgumentException() {
		when(storyRepository.findById(anyInt())).thenReturn(Optional.empty());

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
			storyService.getStoryById("user1", 99);
		});
		assertEquals("指定されたストーリーが見つかりません。", exception.getMessage());

	}

	/**
	 * getStoryByIdメソッドにおいてログインユーザーと保存ユーザーが異なる場合、
	 * セキュリティのためAccessDeniedExceptionが発生することを確認するテスト。
	 */
	@Test
	void getStoryById_AccessDenied_ThrowAccessDeniedException() {
		String myUserId = "user1";
		String hackerUserId = "hacker_user";
		Integer storyId = 99;
		Story mockStory = new Story();
		mockStory.setStoryId(storyId);
		mockStory.setUserId(myUserId);

		when(storyRepository.findById(storyId)).thenReturn(Optional.of(mockStory));

		AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
			storyService.getStoryById(hackerUserId, storyId);
		});
		assertEquals("他のユーザーの保存したストーリーは閲覧できません。", exception.getMessage());
	}

	/**
	 * deleteStoryメソッドにおいてユーザーIDとストーリー情報のユーザーIDが一致する場合、
	 * 保存したストーリーが削除されることを確認するテスト。
	 */
	@Test
	void deleteStory_ValidUserAndId_DeleteFromRepository() {
		String userId = "user1";
		Integer storyId = 99;
		Story mockStory = new Story();
		mockStory.setStoryId(storyId);
		mockStory.setUserId(userId);

		when(storyRepository.findById(storyId)).thenReturn(Optional.of(mockStory));

		storyService.deleteStory(userId, storyId);

		verify(storyRepository).delete(mockStory);
	}

	/**
	 * deleteStoryメソッドにおいてストーリーのデータがデータベースに存在しない場合、
	 * IllegalArgumentExceptionが発生することを確認するテスト。
	 */
	@Test
	void deleteStory_StoryNotFound_ThrowIllegalArgumentException() {
		when(storyRepository.findById(anyInt())).thenReturn(Optional.empty());

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
			storyService.deleteStory("user1", 99);
		});
		assertEquals("指定されたストーリーが見つかりません。", exception.getMessage());

		verify(storyRepository, never()).delete(any(Story.class));
	}

	/**
	 * deleteStoryメソッドにおいてデータベースのユーザーIDとログインユーザーIDが異なる場合、
	 * セキュリティのためにAccessDeniedExceptionが発生することを確認するテスト。
	 */
	@Test
	void deleteStory_AccessDenied_ThrowAccessDeniedException() {
		String myUserId = "user1";
		String hackerUserId = "hacker_user";
		Integer storyId = 99;
		Story mockStory = new Story();
		mockStory.setStoryId(storyId);
		mockStory.setUserId(myUserId);

		when(storyRepository.findById(storyId)).thenReturn(Optional.of(mockStory));

		AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
			storyService.deleteStory(hackerUserId, storyId);
		});
		assertEquals("他のユーザーの保存したストーリーは閲覧できません。", exception.getMessage());

		verify(storyRepository, never()).delete(any(Story.class));
	}
}