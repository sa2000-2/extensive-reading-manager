package com.example.extensivereading.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import com.example.extensivereading.entity.Story;

/**
 * ストーリー保存記録のデータベース操作を担当するRepositoryインターフェース。
 */
public interface StoryRepository extends JpaRepository<Story, Integer> {

	/**
	 * 指定されたユーザーIDに紐づくストーリー保存記録を、保存日の新しい順に取得する。
	 * @param userId 検索対象のユーザーID
	 * @return 保存したストーリーのリスト
	 */
	List<Story> findByUserIdOrderBySavedDateDesc(String userId);

	/**
	 * 指定されたユーザーIDに紐づくストーリー保存記録を削除する。
	 * @param userId 削除対象のユーザーID
	 */
	@Modifying
	void deleteByUserId(String userId);

}
