package com.example.extensivereading.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import com.example.extensivereading.entity.BookRecord;

@Repository
public interface BookRecordRepository extends JpaRepository<BookRecord, Integer>  {

	List<BookRecord> findByUserIdOrderByReadDateDesc(String userId);
	
	@Modifying
	void deleteByUserId(String userId);

}
