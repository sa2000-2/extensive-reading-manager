package com.example.extensivereading.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.extensivereading.dto.BoardForm;
import com.example.extensivereading.entity.Board;
import com.example.extensivereading.repository.BoardRepository;

/**
* 掲示板の投稿に関する処理のServiceクラス
*/
@Service
public class BoardService {
	private final BoardRepository boardRepository;
	
	 public BoardService(BoardRepository boardRepository) {
	        this.boardRepository = boardRepository;
	 }
	 

	 /**
	     * 投稿の登録をする
	     * @param userId ユーザーID
	     * @param form 投稿フォームに入力された投稿情報
	     */
	 @Transactional
	 public void boardRegister(String userId, BoardForm form) {
		Board board = new Board();
	        
	        board.setUserId(userId);
	        board.setText(form.getText().trim());
	        board.setPostDate(LocalDateTime.now());
	        
	        boardRepository.save(board);
	    }

	 /**
	     * 全投稿リストを取得する
	     * @return 投稿日時が新しい順の全ての投稿リスト
	     */
	 public List<Board> getAllBoardList() {
	        return boardRepository.findAllByOrderByPostDateDesc();
	    }
	 

	 /**
	     * ユーザーの投稿リストを取得する
	     * @param userId ユーザーID
	     * @return ユーザーの投稿リスト
	     */
	 public List<Board> getBoardList(String userId) {
	        return boardRepository.findByUserId(userId);
	    }
	 

	 /**
	     * 指定された投稿の情報を取得する
	     * @param userId ユーザーID
	     * @param postId 投稿ID
	     * @return 指定された投稿
	     * @throws IllegalArgumentException 投稿IDが指定されてない場合発生、または投稿IDに紐づく情報がみつからない場合に発生
	     * @throws AccessDeniedException データベースのユーザーIDとユーザーIDが異なる場合に発生
	     */
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
	 
	 /**
	     * 投稿の更新をする
	     * @param userId ユーザーID
	     * @param form 投稿フォームに入力された投稿変更情報
	     */
	    @Transactional
	    public void updateBoard(String userId, BoardForm form) {
	        Board board = getBoard(userId, form.getPostId());

	        board.setText(form.getText().trim());

	        boardRepository.save(board);
	    }
	    
	    /**
	     * 指定された投稿を削除する
	     * @param userId ユーザーID
	     * @param postId 投稿ID
	     */
	    @Transactional
	    public void deleteBoard(String userId, Integer postId) {

	        Board board = getBoard(userId, postId);
	        boardRepository.delete(board);
	    }

}
