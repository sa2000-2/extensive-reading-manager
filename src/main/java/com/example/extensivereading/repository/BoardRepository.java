package com.example.extensivereading.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import com.example.extensivereading.entity.Board;

/**
 * 掲示板投稿のデータベース操作を担当するRepositoryインターフェース。
 */
public interface BoardRepository extends JpaRepository<Board, Integer> {

	/**
	 * 全ての投稿を、投稿日時が新しい順に取得する。
	 * @return 投稿日時の降順で並んだ投稿のリスト
	 */
	List<Board> findAllByOrderByPostDateDesc();

	/**
	 * 指定されたユーザーIDに紐づく投稿記録を投稿日時が新しい順に取得する。
	 * @param userId 検索対象のユーザーID
	 * @return 投稿日時の降順で並んだ投稿のリスト
	 */
	List<Board> findByUserIdOrderByPostDateDesc(String userId);

	/**
	 * 指定されたユーザーIDに紐づく投稿記録を削除する。
	 * @param userId 削除対象のユーザーID
	 */
	@Modifying
	void deleteByUserId(String userId);

}
