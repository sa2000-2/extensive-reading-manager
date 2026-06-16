package com.example.extensivereading.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.example.extensivereading.dto.BookRecordForm;
import com.example.extensivereading.entity.BookRecord;
import com.example.extensivereading.entity.User;
import com.example.extensivereading.repository.BookRecordRepository;
import com.example.extensivereading.repository.UserRepository;

/**
 * BookRecordServiceのテストクラス。
 * 読書記録の登録、更新、削除、および語数・進捗率の計算ロジックのテストを行う。
 */
@ExtendWith(MockitoExtension.class)
public class BookRecordServiceTest {
	@Mock
	private BookRecordRepository bookRecordRepository;
	@Mock
	private UserRepository userRepository;
	
	@InjectMocks
	private BookRecordService bookRecordService;

	/**
	 * getTargetWordsメソッドにおいてユーザーIDから
	 * 登録されている目標語数が正しく取得されることを確認するテスト。
	 */
	@Test
	void getTargetWords_UserExists_ReturnTargetWords() {
		String userId = "user123";
		User user = new User();
		user.setId(userId);
		user.setTargetWords(12345);
		
		when(userRepository.findById("user123")).thenReturn(Optional.of(user));
		
		int actual = bookRecordService.getTargetWords(userId);
		
		assertEquals(12345, actual);
		
	}
	
	/**
	 * getTargetWordsメソッドにおいてユーザーデータが見つからない場合、
	 * NoSuchElementExceptionが発生することを確認するテスト。
	 */
	@Test
	void getTargetWords_UserNotExists_ThrowNoSuchElementException() {
		String userId = "none";
		
		when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> {
            bookRecordService.getTargetWords(userId);
        });
		
	}
	
	
	/**
	 * bookRegisterメソッドにおいて入力された本のタイトルの前後に空白がある場合、
	 * 空白を削除して保存することを確認するテスト。
	 */
	@Test
	void bookRegister_TrimBookTitle() {
		String userId = "user123";
		BookRecordForm form = new BookRecordForm();

		form.setBookTitle("  BookTitle ");
        form.setReadDate(LocalDate.of(2026, 6, 16));
        form.setWordCount(1234);
        
        bookRecordService.bookRegister(userId, form);
        
        ArgumentCaptor<BookRecord> recordCaptor = ArgumentCaptor.forClass(BookRecord.class);
        verify(bookRecordRepository).save(recordCaptor.capture());
		
		BookRecord savedRecord = recordCaptor.getValue();
        assertEquals("BookTitle", savedRecord.getBookTitle());
        assertEquals(userId, savedRecord.getUserId());
        assertEquals(1234, savedRecord.getWordCount());
	}
	
	/**
	 * calculateTotalWordsメソッドにおいて読書記録のリストを渡したとき、
	 * 合計語数が正しく計算されることを確認するテスト。
	 */
	@Test
	void calculateTotalWords_ReturnSumOfWordCounts() {
		List<BookRecord> records = new ArrayList<>();
		
		BookRecord r1 = new BookRecord();
		r1.setWordCount(100);
		
        BookRecord r2 = new BookRecord();
        r2.setWordCount(200);
        
        BookRecord r3 = new BookRecord();
        r3.setWordCount(300);
        
        records.add(r1);
        records.add(r2);
        records.add(r3);
        
        int total = bookRecordService.calculateTotalWords(records);
        assertEquals(600, total);
	}
	
	/**
	 * calculateProgressPercentageメソッドにおいて合計語数と目標語数を渡したとき、
	 * 進捗率の計算が正しく行われることを確認するテスト。
	 */
	@Test
	void calculateProgressPercentage_ReturnCorrectPercentage() {
		double progress = bookRecordService.calculateProgressPercentage(100, 1000);
		assertEquals(10.0, progress, 0.001);
		
	}
	
	/**
	 * calculateProgressPercentageメソッドにおいて目標語数が 0 以下のとき、
	 * 0除算エラーにならず安全に 0.0 が返ってくることを確認するテスト。
	 */
	@Test
	void calculateProgressPercentage_TargetWordsIsZero_ReturnZero() {
		assertEquals(0.0, bookRecordService.calculateProgressPercentage(5000, 0), 0.001);
        assertEquals(0.0, bookRecordService.calculateProgressPercentage(5000, -100), 0.001);
		
	}
	
	
	/**
	 * getRecordメソッドにおいて自分のユーザーIDと正しい記録IDを渡したとき、
	 * その読書記録データが正しく取得されることを確認するテスト。
	 */
	@Test
	void getRecord_ReturnRecordData() {
		String userId ="user123";
		Integer recordId = 1;
		
		BookRecord expectedRecord = new BookRecord();
        expectedRecord.setRecordId(recordId);
        expectedRecord.setUserId(userId);
        expectedRecord.setBookTitle("テスト本");

		when(bookRecordRepository.findById(recordId)).thenReturn(Optional.of(expectedRecord));

        BookRecord actualRecord = bookRecordService.getRecord(userId, recordId);

        assertNotNull(actualRecord);
        assertEquals(userId, actualRecord.getUserId());
        assertEquals("テスト本", actualRecord.getBookTitle());
	}
	
	
	/**
	 * getRecordメソッドにおいて本の記録IDが null だった場合、
	 * IllegalArgumentExceptionが発生することを確認するテスト。
	 */
	@Test
	void getRecord_RecordIdNull_ThrowIllegalArgumentException() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookRecordService.getRecord("user123", null);
        });
		
		assertEquals("記録IDが指定されていません。", exception.getMessage());
		verify(bookRecordRepository, never()).findById(anyInt());
		
	}
	
	
	/**
	 * getRecordメソッドにおいて指定されたIDの記録がデータベースに存在しない場合、
	 * IllegalArgumentExceptionが発生することを確認するテスト。
	 */
	@Test
	void getRecord_RecordIdNotExists_ThrowIllegalArgumentException() {
		Integer recordId = 1;
        when(bookRecordRepository.findById(recordId)).thenReturn(Optional.empty());
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookRecordService.getRecord("user123", recordId);
        });
		
		assertEquals("指定された記録が見つかりません。", exception.getMessage());
		
	}
	
	/**
	 * getRecordメソッドにおいてデータのユーザーIDとログインユーザーIDが異なっていた場合、
	 * セキュリティのために AccessDeniedExceptionが発生することを確認するテスト。
	 */
	@Test
	void getRecord_NotSameUserId_ThrowAccessDeniedException() {
		String userId ="user123";
		String hackerUserId = "hacker_user";
		Integer recordId = 1;
		
		BookRecord victimRecord = new BookRecord();
        victimRecord.setRecordId(recordId);
        victimRecord.setUserId(userId);
		
        when(bookRecordRepository.findById(recordId)).thenReturn(Optional.of(victimRecord));
        
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            bookRecordService.getRecord(hackerUserId, recordId);
        });
        
        assertEquals("他のユーザーの記録は取得できません。", exception.getMessage());
		
	}
	
	/**
	 * updateRecordメソッドにおいて変更する本のタイトルに前後の空白があった場合、
	 * 空白が消されて上書き保存されることを確認するテスト。
	 */
	@Test
	void updateRecord_TrimUpdateBookTitle() {
		String userId = "user123";
        Integer recordId = 1;

        BookRecordForm form = new BookRecordForm();
        form.setRecordId(recordId);
        form.setBookTitle("   BookTitle  ");
        form.setReadDate(LocalDate.of(2026, 6, 16));
        form.setWordCount(500);
        
        BookRecord existingRecord = new BookRecord();
        existingRecord.setRecordId(recordId);
        existingRecord.setUserId(userId);
        existingRecord.setBookTitle("OldTitle");
        
        when(bookRecordRepository.findById(recordId)).thenReturn(Optional.of(existingRecord));
        
        bookRecordService.updateRecord(userId, form);
        
        ArgumentCaptor<BookRecord> recordCaptor = ArgumentCaptor.forClass(BookRecord.class);
        verify(bookRecordRepository).save(recordCaptor.capture());
		
		BookRecord updatedRecord = recordCaptor.getValue();
        assertEquals("BookTitle", updatedRecord.getBookTitle());
	}
	
	/**
	 * updateRecordメソッドにおいて本のタイトルや日時、語数を渡したとき、
	 * 正しく上書き保存されることを確認するテスト。
	 */
	@Test
	void updateRecord_UpdateRecordAndSave() {
		String userId = "user123";
        Integer recordId = 1;

        BookRecordForm form = new BookRecordForm();
        form.setRecordId(recordId);
        form.setBookTitle("NewBookTitle");
        form.setReadDate(LocalDate.of(2026, 6, 16));
        form.setWordCount(500);
        
        BookRecord existingRecord = new BookRecord();
        existingRecord.setRecordId(recordId);
        existingRecord.setUserId(userId);
        existingRecord.setBookTitle("OldBookTitle");
        existingRecord.setReadDate(LocalDate.of(2026, 5, 16));
        existingRecord.setWordCount(1000);
        
        when(bookRecordRepository.findById(recordId)).thenReturn(Optional.of(existingRecord));
        
        bookRecordService.updateRecord(userId, form);

        ArgumentCaptor<BookRecord> recordCaptor = ArgumentCaptor.forClass(BookRecord.class);
        verify(bookRecordRepository).save(recordCaptor.capture());

        BookRecord savedRecord = recordCaptor.getValue();
        assertEquals("NewBookTitle", savedRecord.getBookTitle());
        assertEquals(LocalDate.of(2026, 6, 16), savedRecord.getReadDate());
        assertEquals(500, savedRecord.getWordCount());
		
	}
	
	
	/**
	 * deleteRecordメソッドにおいてデータが削除されることを確認するテスト。
	 */
	@Test
	void deleteRecord_DeleteRecord() {
		String userId = "user123";
        Integer recordId = 1;

        BookRecord existingRecord = new BookRecord();
        existingRecord.setRecordId(recordId);
        existingRecord.setUserId(userId);

        when(bookRecordRepository.findById(recordId)).thenReturn(Optional.of(existingRecord));

        bookRecordService.deleteRecord(userId, recordId);

        verify(bookRecordRepository).delete(existingRecord);
		
	}

}
