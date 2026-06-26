package com.example.extensivereading.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import com.example.extensivereading.entity.BookRecord;

/**
 * 読書記録のデータベース操作を担当するRepositoryインターフェース。
 */
@Repository
public interface BookRecordRepository extends JpaRepository<BookRecord, Integer> {

	/**
	 * 指定されたユーザーIDに紐づく読書記録を、読書日の新しい順に取得する。
	 * @param userId 検索対象のユーザーID
	 * @return 読書日の降順で並んだ読書記録のリスト
	 */
	List<BookRecord> findByUserIdOrderByReadDateDesc(String userId);

	/**
	 * 指定されたユーザーIDに紐づく読書記録をすべて削除する。
	 * @param userId 削除対象のユーザーID
	 */
	@Modifying
	void deleteByUserId(String userId);

}
