package com.example.extensivereading.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.extensivereading.dto.BookRecordForm;
import com.example.extensivereading.entity.BookRecord;
import com.example.extensivereading.entity.User;
import com.example.extensivereading.repository.BookRecordRepository;
import com.example.extensivereading.repository.UserRepository;

/**
 * 読書管理機能に関する処理のServiceクラス
 * 登録、語数計算、更新、削除の処理を行う
 */
@Service
public class BookRecordService {
	private final BookRecordRepository bookRecordRepository;
	private final UserRepository userRepository;
	
	 public BookRecordService(BookRecordRepository bookRecordRepository, UserRepository userRepository) {
	        this.bookRecordRepository = bookRecordRepository;
	        this.userRepository = userRepository;
	 }
	 
	 
	 /**
	     * ユーザー情報のデータベースから目標語数を取得
	     * @param userId ユーザーID
	     * @return ユーザーの目標語数
	     */
	 public int getTargetWords(String userId) {
		 User user = userRepository.findById(userId).orElseThrow();
		 int targetWords = user.getTargetWords();
		 return targetWords;
	 }

	 /**
	     * 読書記録の登録を実行する。
	     * @param userId ユーザーID
	     * @param form 登録画面から入力された読書記録の情報
	     */
	 @Transactional
	 public void bookRegister(String userId, BookRecordForm form) {
		BookRecord record = new BookRecord();

	        record.setUserId(userId);
	        record.setBookTitle(form.getBookTitle().trim());
	        record.setReadDate(form.getReadDate());
	        record.setWordCount(form.getWordCount());
	        
	        bookRecordRepository.save(record);
	    }
	 
	 /**
	     * 読書記録のリストを取得する。
	     * @param userId ユーザーID
	     * @return 読んだ日付が新しい順に並んだ読書記録情報のリスト
	     */
	 public List<BookRecord> getAllRecords(String userId) {
	        return bookRecordRepository.findByUserIdOrderByReadDateDesc(userId);
	    }
	 
	 /**
	     * 合計語数を計算する
	     * * @param records 読書記録の情報リスト
	     * @return ユーザーの合計語数
	     */
	 public int calculateTotalWords(List<BookRecord> records) {
	        return records.stream()
	                      .mapToInt(BookRecord::getWordCount)
	                      .sum();
	    }
	 
	 
	 /**
	     * 目標語数への進捗%を計算する
	     * * @param totalWords ユーザーの合計語数
	     * @param targetWords ユーザーの目標語数
	     * @return 目標語数への進捗%
	     */
	    public double calculateProgressPercentage(int totalWords, int targetWords) {
	        if (targetWords <= 0) {
	            return 0.0;
	        }
	        return ((double) totalWords / targetWords) * 100;
	    }

	    
	    /**
	     * データベースからユーザーの読書記録の情報を取得する
	     * * @param userId ユーザーID
	     * @param　recordId 本の記録ID
	     * @return データベースのユーザーの読書記録の情報
	     * @throws IllegalArgumentException 指定した記録がない場合
	     * @throws AccessDeniedException データベースのユーザーIDとユーザーIDが異なる場合に発生
	     */
	    public BookRecord getRecord(String userId, Integer recordId) {
	    	if (recordId == null) {
	            throw new IllegalArgumentException("記録IDが指定されていません。");
	        }
	        BookRecord record = bookRecordRepository.findById(recordId)
	                .orElseThrow(() -> new IllegalArgumentException("指定された記録が見つかりません。"));

	        if (!record.getUserId().equals(userId)) {
	            throw new AccessDeniedException("他のユーザーの記録は取得できません。");
	        }

	        return record;
	    }
	 
	    /**
	     * 編集画面での更新データを保存する
	     * @param userId　ユーザーID
	     * @param　form 変更画面で入力された読書記録の情報
	     */
	    @Transactional
	    public void updateRecord(String userId, BookRecordForm form) {
	        BookRecord record = getRecord(userId, form.getRecordId());

	        record.setBookTitle(form.getBookTitle().trim());
	        record.setReadDate(form.getReadDate());
	        record.setWordCount(form.getWordCount());

	        bookRecordRepository.save(record);
	    }
	    
	    /**
	     * 読書記録の削除
	     * @param userId　ユーザーID
	     * @param　recordId　本の記録ID
	     */
	    @Transactional
	    public void deleteRecord(String userId, Integer recordId) {
	        BookRecord record = getRecord(userId, recordId);
	        bookRecordRepository.delete(record);
	    }

}
