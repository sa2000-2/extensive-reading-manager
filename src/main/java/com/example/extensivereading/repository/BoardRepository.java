package com.example.extensivereading.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import com.example.extensivereading.entity.Board;

public interface BoardRepository extends JpaRepository<Board, Integer>{
	
	/**
	 * 全ての投稿を、投稿日時が新しい順に取得する。
	 * @return 投稿記録のリスト
	 */
    List<Board> findAllByOrderByPostDateDesc();
    
    /**
	 * 指定されたユーザーIDに紐づく投稿記録を取得する。
	 * @param userId 検索するユーザーID
	 * @return 投稿記録のリスト
	 */
    List<Board> findByUserId(String userId);
    
    /**
	 * 指定されたユーザーIDに紐づく投稿記録を削除する
	 * @param userId 削除するユーザーID
	 */
    @Modifying
    void deleteByUserId(String userId);


}
