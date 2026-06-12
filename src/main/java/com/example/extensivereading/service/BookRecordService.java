package com.example.extensivereading.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.extensivereading.dto.BookRecordForm;
import com.example.extensivereading.entity.BookRecord;
import com.example.extensivereading.repository.BookRecordRepository;

@Service
public class BookRecordService {
	private final BookRecordRepository bookRecordRepository;
	
	 public BookRecordService(BookRecordRepository bookRecordRepository) {
	        this.bookRecordRepository = bookRecordRepository;
	 }
	 

	 @Transactional
	 public void bookRegister(String userId, BookRecordForm form) {
		BookRecord record = new BookRecord();

	        record.setUserId(userId);
	        record.setBookTitle(form.getBookTitle().trim());
	        record.setReadDate(form.getReadDate());
	        record.setWordCount(form.getWordCount());
	        
	        bookRecordRepository.save(record);
	    }
	 
	 public List<BookRecord> getAllRecords(String userId) {
	        return bookRecordRepository.findByUserIdOrderByReadDateDesc(userId);
	    }
	 
	 
	 public int calculateTotalWords(List<BookRecord> records) {
	        return records.stream()
	                      .mapToInt(BookRecord::getWordCount)
	                      .sum();
	    }
	 
	    public double calculateProgressPercentage(int totalWords, int targetWords) {
	        // ゼロ除算（0で割るエラー）を防ぐための論理チェック
	        if (targetWords <= 0) {
	            return 0.0;
	        }
	        return ((double) totalWords / targetWords) * 100;
	    }

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
	 
	    @Transactional
	    public void updateRecord(String userId, BookRecordForm form) {
	        BookRecord record = getRecord(userId, form.getRecordId());

	        record.setBookTitle(form.getBookTitle().trim());
	        record.setReadDate(form.getReadDate());
	        record.setWordCount(form.getWordCount());

	        bookRecordRepository.save(record);
	    }
	    
	    @Transactional
	    public void deleteRecord(String userId, Integer recordId) {
	        BookRecord record = getRecord(userId, recordId);
	        bookRecordRepository.delete(record);
	    }

}
