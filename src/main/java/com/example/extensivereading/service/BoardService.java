package com.example.extensivereading.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.extensivereading.dto.BoardForm;
import com.example.extensivereading.entity.Board;
import com.example.extensivereading.repository.BoardRepository;

@Service
public class BoardService {
	private final BoardRepository boardRepository;
	
	 public BoardService(BoardRepository boardRepository) {
	        this.boardRepository = boardRepository;
	 }
	 

	 @Transactional
	 public void boardRegister(String userId, BoardForm form) {
		Board board = new Board();
	        
	        board.setUserId(userId);
	        board.setText(form.getText().trim());
	        board.setPostDate(LocalDateTime.now());
	        
	        boardRepository.save(board);
	    }

	 public List<Board> getAllBoardList() {
	        return boardRepository.findAllByOrderByPostDateDesc();
	    }
	 

	 public List<Board> getBoardList(String userId) {
	        return boardRepository.findByUserId(userId);
	    }
	 

	    public Board getBoard(String userId, Integer postId) {
	        if (postId == null) {
	            throw new IllegalArgumentException("投稿IDが指定されていません。");
	        }

	        Board board = boardRepository.findById(postId)
	                .orElseThrow(() -> new IllegalArgumentException("指定された投稿が見つかりません。"));

	        if (!board.getUserId().equals(userId)) {
	            throw new AccessDeniedException("他のユーザーの投稿は取得できません。");
	        }

	        return board;
	    }
	 
	    @Transactional
	    public void updateBoard(String userId, BoardForm form) {
	        Board board = getBoard(userId, form.getPostId());

	        board.setText(form.getText().trim());

	        boardRepository.save(board);
	    }
	    
	    @Transactional
	    public void deleteBoard(String userId, Integer postId) {

	        Board board = getBoard(userId, postId);
	        boardRepository.delete(board);
	    }

}
