package com.example.extensivereading.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.extensivereading.dto.FavoriteForm;
import com.example.extensivereading.dto.RecommendResponse;
import com.example.extensivereading.entity.Favorite;
import com.example.extensivereading.repository.FavoriteRepository;

/**
* AIおすすめ本表示機能に関する処理のServiceクラス
* AIとの通信や返答の処理、保存、削除を担当する
*/
@Service
public class RecommendService {

    private final ChatClient chatClient;
    private final FavoriteRepository favoriteRepository;
    private static final List<String> ALLOWED_TYPES = List.of("Graded Readers", "General Books");
    private static final List<String> ALLOWED_LEVELS = List.of("1", "2", "3", "4", "5", "6", "7", "8", "9");
    
    
    /**
     * AI推薦で許可するジャンル一覧
     * リクエスト改ざんによる不正なジャンル指定を防ぐために使用する。
     */
    public enum AllowedGenre {
        Mystery, Fantasy, Romance, SciFi, Action, NonFiction;

    	
    	/**
    	 * 指定されたジャンルが許可リストに含まれているか判定する。
    	 *
    	 * @param genre ユーザーが選択したジャンル
    	 * @return 許可されていない場合 true
    	 */
        public static boolean isInvalid(String genre) {
        	
        	if (genre == null || genre.isBlank()) {
                return true;
            }
        	
            String cleanGenre = genre.replace("-", "").trim();
            for (AllowedGenre g : values()) {
                if (g.name().equalsIgnoreCase(cleanGenre)) return false;
            }
            return true;
        }
    }

    public RecommendService(ChatClient.Builder chatClientBuilder, FavoriteRepository favoriteRepository) {
        this.chatClient = chatClientBuilder.build();
        this.favoriteRepository = favoriteRepository;
    }

    
    /**
     * 選択された条件で出力させたAIのおすすめ本の提案リストを取得する
     * @param level ユーザーが選択したレベル
     * @param type ユーザーが選択した本の種類
     * @param genre ユーザーが選択したジャンル
     * @return おすすめ本のリスト
     */
    public List<RecommendResponse> executeRecommend(String level, String type, String genre) {
    	
    	if (level == null || !ALLOWED_LEVELS.contains(level.trim())) {
            throw new IllegalArgumentException("不正なレベルが選択されました。");
        }

        if (type == null || !ALLOWED_TYPES.contains(type.trim())) {
            throw new IllegalArgumentException("不正な書籍タイプが選択されました。");
        }

        if (AllowedGenre.isInvalid(genre)) {
            throw new IllegalArgumentException("不正なジャンルが選択されました。");
        }
        
        String typeInstruction = type.equals("Graded Readers") ?
                """
                * 書籍タイプ: 多読用書籍（Graded Readers）
                * レベル基準 (%s/9): 
                  - レベル1〜2: 各レーベルの Starter や Stage 1（語彙数 250〜400語前後）
                  - レベル3〜4: 各レーベルの Stage 2〜3（語彙数 600〜1000語前後）
                  - レベル5〜6: 各レーベルの Stage 4〜5（語彙数 1400〜2000語前後）
                  - レベル7〜9: 各レーベルの最高峰 Stage 6 や Level 7（語彙数 2500語〜3000語以上)
                """.formatted(level)
                : 
                """
                * 書籍タイプ: 普通の本（一般書・ペーパーバック・絵本・児童書・小説）、Graded Readersを除く
                * レベル基準 (%s/9): 
                  - レベル1〜2: 簡単な絵本・児童書（例: 'Frog and Toad'）
                  - レベル3〜4: 読みやすいチャプターブック（例: 'Magic Tree House'）
                  - レベル5〜6: 中高生向けの児童文学やYA小説（例: 'Holes'）
                  - レベル7: 大人向けの簡単なペーパーバック
                  - レベル8: 一般的な小説、サスペンス
                  - レベル9: 本格的な文学作品、難解な古典
                """.formatted(level);

    
        String promptText = """
                あなたは英語多読の専門家です。英語の多読をしている人におすすめの洋書を【厳密に3冊】厳選してください。
                
                【ユーザーの選択条件】
                %s
                * 希望するジャンル: %s
                
                【出力ルール】
                1. 選択されたレベル基準に【厳格に一致する本】を、希望ジャンルから重複なく3冊選ぶこと。
                2. あらすじ（summary）は、日本語で100文字程度で記述すること。
                3. おすすめの理由（reason）は、日本語で100文字程度で記述すること。
                4. レスポンスは、必ず以下のJSONフォーマット配列のみで返却し、余計な挨拶やマークダウンは一切含めないこと。
                5. 出版社（publisher）には、「Cambridge University book」のような曖昧な名称は絶対に避け、
                特に多読用書籍（Graded Readers）の場合は『Cambridge English Readers』や『Oxford Bookworms』などの
                【正確なシリーズ名・レーベル名】を記述すること。一般書の場合は『Penguin Books』などの正式な出版社名を記述すること。
                
                [
                  {"title": "本のタイトル", "author": "著者名", "publisher": "シリーズ名または出版社名", "summary": "日本語あらすじ", "reason": "日本語のおすすめ理由"},
                  {"title": "本のタイトル", "author": "著者名", "publisher": "シリーズ名または出版社名", "summary": "日本語あらすじ", "reason": "日本語のおすすめ理由"},
                  {"title": "本のタイトル", "author": "著者名", "publisher": "シリーズ名または出版社名", "summary": "日本語あらすじ", "reason": "日本語のおすすめ理由"}
                ]
                """.formatted(typeInstruction, genre);
 

        BeanOutputConverter<List<RecommendResponse>> converter = 
            new BeanOutputConverter<>(new ParameterizedTypeReference<List<RecommendResponse>>() {});

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

        List<RecommendResponse> recommendList = converter.convert(responseText);
        
        if (recommendList == null || recommendList.isEmpty()) {
            throw new RuntimeException("AIから有効なおすすめ書籍を取得できませんでした。");
        }

        if (recommendList.size() > 3) {
            recommendList = recommendList.subList(0, 3);
        }

        for (RecommendResponse book : recommendList) {
            
            if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
                book.setTitle("AIおすすめの書籍");
            } else if (book.getTitle().length() > 100) {
                book.setTitle(book.getTitle().substring(0, 100) + "…");
            }

            if (book.getAuthor() == null || book.getAuthor().trim().isEmpty()) {
                book.setAuthor("Unknown");
            } else if (book.getAuthor().length() > 100) {
                book.setAuthor(book.getAuthor().substring(0, 100) + "…");
            }

            if (book.getPublisher() == null || book.getPublisher().trim().isEmpty()) {
                book.setPublisher("不明");
            } else if (book.getPublisher().length() > 100) {
                book.setPublisher(book.getPublisher().substring(0, 100) + "…");
            }

            if (book.getSummary() == null || book.getSummary().trim().isEmpty()) {
                book.setSummary("詳細情報はありません。");
            } else if (book.getSummary().length() > 400) {
                book.setSummary(book.getSummary().substring(0, 400) + "…");
            }
            
            if (book.getReason() == null || book.getReason().trim().isEmpty()) {
                book.setReason("おすすめの理由はありません。");
            } else if (book.getReason().length() > 200) {
                book.setReason(book.getReason().substring(0, 200) + "…");
            }
        }

        return recommendList;
    }

  
    /**
     * おすすめ本をお気に入り登録する
     * @param userId ユーザーID
     * @param form お気に入り登録するおすすめ本のデータの箱
     */
    @Transactional
    public void saveFavorite(String userId, FavoriteForm form) {
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setBookTitle(form.getTitle().trim());
        favorite.setAuthor(form.getAuthor().trim());
        favorite.setPublisher(form.getPublisher().trim());
        favorite.setSummary(form.getSummary().trim());
        favorite.setSavedDate(LocalDateTime.now());
        favoriteRepository.save(favorite);
    }

    /**
     * お気に入り登録したおすすめ本のリストを取得する
     * @param userId ユーザーID
     * @return データベースから取得したユーザーIDに紐づいたおすすめ本のデータのリスト
     */
    public List<Favorite> getFavorites(String userId) {
        return favoriteRepository.findByUserIdOrderBySavedDateDesc(userId);
    }

    
    /**
     * お気に入り登録したおすすめ本を削除する
     * @param userId ユーザーID
     * @param favoriteId お気に入り登録ID
     */
    @Transactional
    public void deleteFavorite(String userId, Integer favoriteId) {
        Favorite favorite = favoriteRepository.findById(favoriteId)
                .orElseThrow(() -> new IllegalArgumentException("指定されたお気に入りが見つかりません。"));

        if (!favorite.getUserId().equals(userId)) {
            throw new AccessDeniedException("他のユーザーのデータは削除できません。");
        }
        favoriteRepository.delete(favorite);
    }
}