package com.example.extensivereading.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.example.extensivereading.dto.BoardForm;
import com.example.extensivereading.entity.Board;
import com.example.extensivereading.repository.BoardRepository;

/**
 * BoardServiceのテストクラス。
 * 掲示板への新規投稿、一覧取得、特定投稿の取得・バリデーション、更新、削除のテストを行う。
 */
@ExtendWith(MockitoExtension.class)
public class BoardServiceTest {
	@Mock
	private BoardRepository boardRepository;
	
	@InjectMocks
	private BoardService boardService;

	
	/**
	 * boardRegisterメソッドにおいて投稿内容が正常に登録されることを確認するテスト。
	 */
	@Test
	void boardRegister_SaveBoard() {
		String userId="1234";
		BoardForm form = new BoardForm();
		form.setText("テストです");
		
		boardService.boardRegister(userId, form);
		
		ArgumentCaptor<Board> boardCaptor = ArgumentCaptor.forClass(Board.class);
		verify(boardRepository).save(boardCaptor.capture());

		Board savedBoard = boardCaptor.getValue();
		assertEquals(userId, savedBoard.getUserId());
		assertEquals("テストです", savedBoard.getText());
		assertNotNull(savedBoard.getPostDate());
		
	}
	
	/**
	 * boardRegisterメソッドにおいて投稿テキストに前後の空白がある場合、
	 * 空白が削除されて保存されることを確認するテスト。
	 */
	@Test
	void boardRegister_TrimText() {
		String userId="1234";
		BoardForm form = new BoardForm();
		form.setText("  テストです       ");
		
		boardService.boardRegister(userId, form);
		
		ArgumentCaptor<Board> boardCaptor = ArgumentCaptor.forClass(Board.class);
		verify(boardRepository).save(boardCaptor.capture());

		Board savedBoard = boardCaptor.getValue();
		assertEquals("テストです", savedBoard.getText());
		
	}
	
	/**
	 * getAllBoardListメソッドにおいて日付が新しい順で全ての投稿リストが取得されることを確認するテスト。
	 */
	@Test
	void getAllBoardList_ReturnAllList() {
		List<Board> mockList = new ArrayList<>();
		
		Board b1 = new Board();
		b1.setText("投稿1");
		
		Board b2 = new Board();
		b2.setText("投稿2");
		
		mockList.add(b1);
		mockList.add(b2);
		
		when(boardRepository.findAllByOrderByPostDateDesc()).thenReturn(mockList);

		List<Board> actualList = boardService.getAllBoardList();

		assertNotNull(actualList);
		assertEquals(2, actualList.size());
		assertEquals("投稿1", actualList.get(0).getText());
		
		verify(boardRepository).findAllByOrderByPostDateDesc();
	}
	
	/**
	 * getBoardListメソッドにおいて指定されたユーザーIDの投稿リストのみが取得されることを確認するテスト。
	 */
	@Test
	void getBoardList_ReturnUserBoardList() {
		String userId = "user123";
		List<Board> mockList = new ArrayList<>();
		Board b1 = new Board();
		b1.setUserId(userId);
		b1.setText("自分の投稿");
		mockList.add(b1);
		
		when(boardRepository.findByUserIdOrderByPostDateDesc(userId)).thenReturn(mockList);
		
		List<Board> actualList = boardService.getBoardList(userId);

		assertNotNull(actualList);
		assertEquals(1, actualList.size());
		assertEquals("自分の投稿", actualList.get(0).getText());
		assertEquals(userId, actualList.get(0).getUserId());
		
	}
	
	/**
	 * getBoardメソッドにおいてユーザーIDと投稿IDを渡したとき、投稿データが正しく取得されることを確認するテスト。
	 */
	@Test
	void getBoard_ValidUserAndPostId_ReturnBoard() {
		String userId = "user123";
		Integer postId = 1;
		
		Board expectedBoard = new Board();
		expectedBoard.setPostId(postId);
		expectedBoard.setUserId(userId);
		expectedBoard.setText("確認用の投稿");
		
		when(boardRepository.findById(postId)).thenReturn(Optional.of(expectedBoard));
		
		Board actualBoard = boardService.getBoard(userId, postId);
		
		assertNotNull(actualBoard);
		assertEquals(postId, actualBoard.getPostId());
		assertEquals(userId, actualBoard.getUserId());
		assertEquals("確認用の投稿", actualBoard.getText());
		
	}
	
	/**
	 * getBoardメソッドにおいて投稿IDがnullのとき、IllegalArgumentExceptionが発生することを確認するテスト。
	 */
	@Test
	void getBoard_PostIdNull_ThrowIllegalArgumentException() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            boardService.getBoard("user123", null);
        });
		
		assertEquals("投稿IDが指定されていません。", exception.getMessage());
		verify(boardRepository, never()).findById(anyInt());
		
	}
	
	/**
	 * getBoardメソッドにおいて投稿IDのデータがデータベースに存在しない場合、
	 * IllegalArgumentExceptionが発生することを確認するテスト。
	 */
	@Test
	void getBoard_PostIdNotExists_ThrowIllegalArgumentException() {
		Integer postId = 1;
        when(boardRepository.findById(postId)).thenReturn(Optional.empty());
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            boardService.getBoard("user123", postId);
        });
		
		assertEquals("指定された投稿が見つかりません。", exception.getMessage());
	}
	
	/**
	 * getBoardメソッドにおいてデータのユーザーIDとログインユーザーIDが異なる場合、
	 * セキュリティのためにAccessDeniedExceptionが発生することを確認するテスト。
	 */
	@Test
	void getBoard_NotSameUserId_ThrowAccessDeniedException() {
		String userId ="user123";
		String hackerUserId = "hacker_user";
		Integer postId = 1;
		
		Board victimBoard = new Board();
        victimBoard.setPostId(postId);
        victimBoard.setUserId(userId);
		
        when(boardRepository.findById(postId)).thenReturn(Optional.of(victimBoard));
        
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            boardService.getBoard(hackerUserId, postId);
        });
        
        assertEquals("他のユーザーの投稿は取得できません。", exception.getMessage());
		
	}
	
	/**
	 * updateBoardメソッドにおいて変更テキストの前後に空白がある場合、
	 * 空白が削除され上書き保存されることを確認するテスト。
	 */
	@Test
	void updateBoard_TrimText() {
		String userId = "user123";
		Integer postId = 1;

		BoardForm form = new BoardForm();
		form.setPostId(postId);
		form.setText("   編集後テキスト     ");

		Board existingBoard = new Board();
		existingBoard.setPostId(postId);
		existingBoard.setUserId(userId);
		existingBoard.setText("編集前テキスト");
		
		when(boardRepository.findById(postId)).thenReturn(Optional.of(existingBoard));
		boardService.updateBoard(userId, form);
		
		ArgumentCaptor<Board> boardCaptor = ArgumentCaptor.forClass(Board.class);
		verify(boardRepository).save(boardCaptor.capture());

		Board updatedBoard = boardCaptor.getValue();
		assertEquals("編集後テキスト", updatedBoard.getText());
	}
	
	/**
	 * deleteBoardメソッドにおいてデータが削除されることを確認するテスト。
	 */
	@Test
	void deleteBoard_DeleteBoard() {
		String userId = "user123";
        Integer postId = 1;

        Board existingBoard = new Board();
        existingBoard.setPostId(postId);
        existingBoard.setUserId(userId);

        when(boardRepository.findById(postId)).thenReturn(Optional.of(existingBoard));

        boardService.deleteBoard(userId, postId);

        verify(boardRepository).delete(existingBoard);
		
		
	}
}
