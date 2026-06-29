package com.example.extensivereading.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import com.example.extensivereading.entity.Favorite;

/**
 * お気に入り登録記録のデータベース操作を担当するRepositoryインターフェース。
 */
public interface FavoriteRepository extends JpaRepository<Favorite, Integer> {
	/**
	 * 指定されたユーザーIDに紐づくお気に入り登録記録を、保存日の新しい順に取得する。
	 * @param userId 検索対象のユーザーID
	 * @return お気に入り登録のリスト
	 */
	List<Favorite> findByUserIdOrderBySavedDateDesc(String userId);

	/**
	 * 指定されたユーザーIDに紐づくお気に入り登録記録を削除する。
	 * @param userId 削除対象のユーザーID
	 */
	@Modifying
	void deleteByUserId(String userId);

}
