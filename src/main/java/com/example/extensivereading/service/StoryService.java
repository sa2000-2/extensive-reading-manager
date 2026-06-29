package com.example.extensivereading.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.extensivereading.dto.StoryResponse;
import com.example.extensivereading.entity.BookRecord;
import com.example.extensivereading.entity.Story;
import com.example.extensivereading.repository.BookRecordRepository;
import com.example.extensivereading.repository.StoryRepository;

/**
 * AIストーリー生成機能に関する処理を行うServiceクラス。
 * AIとの通信によるストーリー生成、ストーリーの保存・取得・削除、読書記録への登録を担当する。
 */
@Service
public class StoryService {
	private final ChatClient chatClient;
	private final StoryRepository storyRepository;
	private final BookRecordRepository bookRecordRepository;
	private static final List<String> ALLOWED_LEVELS = List.of("1", "2", "3", "4", "5", "6", "7", "8", "9");
	private static final List<String> ALLOWED_WORDCOUNT = List.of("100", "200", "300", "400", "500");

	/**
	 * AIストーリー生成で許可するジャンル一覧。
	 * リクエスト改ざんによる不正なジャンル指定を防ぐために使用する。
	 */
	public enum AllowedGenre {
		Mystery, Fantasy, Romance, SciFi, Action, NonFiction;

		public static boolean isInvalid(String genre) {

			if (genre == null || genre.isBlank()) {
				return true;
			}

			String cleanGenre = genre.replace("-", "").trim();
			for (AllowedGenre g : values()) {
				if (g.name().equalsIgnoreCase(cleanGenre))
					return false;
			}
			return true;
		}
	}

	public StoryService(ChatClient.Builder chatClientBuilder, StoryRepository storyRepository,
			BookRecordRepository bookRecordRepository) {
		this.chatClient = chatClientBuilder.build();
		this.storyRepository = storyRepository;
		this.bookRecordRepository = bookRecordRepository;
	}

	/**
	 * 指定された条件をもとにAIでストーリーを生成する。
	 * @param level ユーザーが選択したレベル
	 * @param wordCount ユーザーが選択した語数
	 * @param genre ユーザーが選択したジャンル
	 * @return AIが生成したストーリー
	 */
	public StoryResponse executeGenerate(String level, String wordCount, String genre) {

		if (level == null || !ALLOWED_LEVELS.contains(level.trim())) {
			throw new IllegalArgumentException("不正なレベルが選択されました。");
		}

		if (wordCount == null || !ALLOWED_WORDCOUNT.contains(wordCount.trim())) {
			throw new IllegalArgumentException("不正な語数が選択されました。");
		}

		if (AllowedGenre.isInvalid(genre)) {
			throw new IllegalArgumentException("不正なジャンルが選択されました。");
		}

		String promptText = """
				あなたは多読用洋書のプロの作家です。
				ユーザーが選択した「英語の難易度（レベル）」と「物語の長さ（語数）」、および「ジャンル」を厳格に守り、素晴らしい英語の物語を1つ執筆してください。

				「wordCount」の算出ルール：
				    物語の執筆がすべて完了したあとに、必ず以下のステップを脳内で実行して正確な数字を入れてください。
				    ステップ1：生成した「contents」の文字列を、半角スペースのみで綺麗に分割（スプリット）する。
				    ステップ2：分割された純粋な英単語の個数（要素数）を、1つずつ「1, 2, 3...」と最後まで真面目にカウントする。
				    ステップ3：そのカウントした本物の合計数字のみを「wordCount」の文字列として格納すること（適当な予想の数字は絶対に禁止します）。


				【ユーザーの選択条件】

				1. 英語の難易度（レベル基準）: レベル %s / 9
				※重要：レベル1からレベル9に向かって、単語の難易度（使用語彙）、文の長さ、および文法構造の複雑さを段階的に（徐々に）引き上げて執筆してください
				   - レベル1〜2: スターター・初級レベル（中学英語・基本語彙のみ。文構造は極めてシンプルに）
				   - レベル3〜4: 初中級レベル（高校基礎英語。少し動きのある表現を許可）
				   - レベル5〜6: 中級レベル（日常会話〜ニュースレベルの一般的な英語）
				   - レベル7〜9: 上級・最高峰レベル（ネイティブ向けの小説に近い豊かな語彙と表現）
				   ※指定されたレベルにふさわしい語彙（単語の難易度）と言語構造を厳守してください。

				2. 希望するジャンル: %s

				3. 物語全体の長さ（希望語数）: %s 単語
				   ※全体の単語数が、この指定語数（誤差±20単語以内）に収まるように物語の展開をコントロールしてください。

				【執筆・出力ルール】
				1. 指定されたレベル、ジャンル、長さに完璧に合致するオリジナルの英語の物語（Story）を1つ作ること。
				2. 物語には必ず魅力的なタイトルを付けること。
				3. 物語の英語レベルに完全に合致した、自然で美しい日本語訳（Japanese translation）も同時に作成すること。
				4. 「wordCount」の項目には、AIが実際に執筆した英語本文の純粋な単語数（半角スペース区切りの単語カウント数）の数字のみを格納すること。
				5. レスポンスは、必ず以下のJSONフォーマット（単一のオブジェクト）のみで返却すること。余計な挨拶、説明文、マークダウン（```json などの囲み）は一切含めず、最初の1文字目は必ず { で始めること。

				{"title": "本のタイトル", "contents": "本文", "wordCount": "語数", "japanese": "日本語訳"}
				"""
				.formatted(level, genre, wordCount);

		BeanOutputConverter<StoryResponse> converter = new BeanOutputConverter<>(StoryResponse.class);

		String formatInstructions = converter.getFormat();
		String responseText;

		try {
			responseText = chatClient.prompt()
					.user(promptText + "\n\n" + formatInstructions)
					.call()
					.content();
		} catch (Exception e) {
			throw new RuntimeException("AIからの応答取得に失敗しました。時間をおいて再度お試しください。", e);
		}

		StoryResponse story = converter.convert(responseText);

		if (story == null || story.getTitle() == null || story.getTitle().trim().isEmpty()) {
			throw new RuntimeException("AIから有効なストーリーを取得できませんでした。");
		}

		if (story.getTitle().length() > 100) {
			story.setTitle(story.getTitle().substring(0, 100) + "…");
		}

		if (story.getContents() == null || story.getContents().trim().isEmpty()) {
			story.setContents("無");
		} else if (story.getContents().length() > 40000) {
			story.setContents(story.getContents().substring(0, 40000) + "…");
		}

		if (story.getWordCount() == null || story.getWordCount().trim().isEmpty()) {
			story.setWordCount("不明");
		} else if (story.getWordCount().length() > 1000) {
			story.setWordCount(story.getWordCount().substring(0, 1000) + "…");
		}

		if (story.getJapanese() == null || story.getJapanese().trim().isEmpty()) {
			story.setJapanese("無");
		} else if (story.getJapanese().length() > 40000) {
			story.setJapanese(story.getJapanese().substring(0, 40000) + "…");
		}

		return story;
	}

	/**
	 * 生成したストーリーの語数を計算して読書記録として保存する。
	 * @param userId ユーザーID
	 * @param response 保存するストーリー情報
	 */
	@Transactional
	public void registerRead(String userId, StoryResponse response) {
		BookRecord record = new BookRecord();
		record.setUserId(userId);
		record.setBookTitle(response.getTitle().trim());
		
		int accurateWordCount = 0;
		if (response.getContents() != null && !response.getContents().isBlank()) {
			String text = response.getContents().trim();
			String cleanText = text.replaceAll("[^a-zA-Z0-9\\s'-]", "");
			String[] words = cleanText.split("\\s+");
			accurateWordCount = words.length;
		}
		
		record.setWordCount(accurateWordCount);
		record.setReadDate(LocalDate.now());
		bookRecordRepository.save(record);
	}

	/**
	 * ストーリーをデータベースに保存する。
	 * @param userId ユーザーID
	 * @param response 保存するストーリー情報
	 */
	@Transactional
	public void saveStory(String userId, StoryResponse response) {
		Story story = new Story();
		story.setUserId(userId);
		story.setTitle(response.getTitle().trim());
		story.setContents(response.getContents().trim());
		story.setWordCount(response.getWordCount().trim());
		story.setJapanese(response.getJapanese().trim());
		story.setSavedDate(LocalDateTime.now());
		storyRepository.save(story);
	}

	/**
	 * 保存したストーリーリストを取得する。
	 * @param userId ユーザーID
	 * @return 指定したユーザーに紐づくストーリー一覧
	 */
	public List<Story> getStory(String userId) {
		return storyRepository.findByUserIdOrderBySavedDateDesc(userId);

	}

	/**
	 * IDを指定して、保存されたストーリーを1件だけ取得する。
	 * @param userId ユーザーID
	 * @param storyId ストーリーID
	 * @return 取得したストーリー
	 */
	public Story getStoryById(String userId, Integer storyId) {
		Story story = storyRepository.findById(storyId)
				.orElseThrow(() -> new IllegalArgumentException("指定されたストーリーが見つかりません。"));

		if (!story.getUserId().equals(userId)) {
			throw new AccessDeniedException("他のユーザーの保存したストーリーは閲覧できません。");
		}
		return story;
	}

	/**
	 * 保存したストーリーを削除する。
	 * @param userId ユーザーID
	 * @param storyId ストーリーID
	 */
	@Transactional
	public void deleteStory(String userId, Integer storyId) {
		Story story = getStoryById(userId, storyId);
		storyRepository.delete(story);

	}
}
