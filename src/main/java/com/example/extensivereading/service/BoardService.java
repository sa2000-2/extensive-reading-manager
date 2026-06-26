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
 * 掲示板投稿に関する業務処理を担当するServiceクラス。
 * 投稿の登録、取得、更新、削除を行う。
 */
@Service
public class BoardService {
	private final BoardRepository boardRepository;

	public BoardService(BoardRepository boardRepository) {
		this.boardRepository = boardRepository;
	}

	/**
	 * 投稿を登録をする。
	 * @param userId ユーザーID
	 * @param form 投稿フォームに入力された投稿内容
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
	 * 全ての投稿を取得する。
	 * @return 投稿日時の新しい順に並んだ投稿のリスト
	 */
	public List<Board> getAllBoardList() {
		return boardRepository.findAllByOrderByPostDateDesc();
	}

	/**
	 * 指定された投稿の情報を取得する。
	 * @param userId ユーザーID
	 * @param postId 投稿ID
	 * @return 指定された投稿
	 * @throws IllegalArgumentException 投稿IDが指定されていない場合、または指定された投稿が存在しない場合
	 * @throws AccessDeniedException 投稿の所有者とログイン中のユーザーが一致しない場合
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
	 * 投稿を更新をする。
	 * @param userId ユーザーID
	 * @param form 投稿フォームに入力された変更内容
	 */
	@Transactional
	public void updateBoard(String userId, BoardForm form) {
		Board board = getBoard(userId, form.getPostId());

		board.setText(form.getText().trim());

		boardRepository.save(board);
	}

	/**
	 * 指定された投稿を削除する。
	 * @param userId ユーザーID
	 * @param postId 投稿ID
	 */
	@Transactional
	public void deleteBoard(String userId, Integer postId) {

		Board board = getBoard(userId, postId);
		boardRepository.delete(board);
	}

	// 現在は未使用。マイページの投稿一覧表示で利用予定。

	/**
	 * 指定されたユーザーの投稿一覧を取得する。
	 * @param userId ユーザーID
	 * @return 投稿日時の新しい順に並んだ投稿のリスト
	 */

	public List<Board> getBoardList(String userId) {
		return boardRepository.findByUserIdOrderByPostDateDesc(userId);
	}
}
