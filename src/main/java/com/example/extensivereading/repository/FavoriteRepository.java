package com.example.extensivereading.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import com.example.extensivereading.entity.Favorite;

public interface FavoriteRepository extends JpaRepository<Favorite, Integer> {
	List<Favorite> findByUserIdOrderBySavedDateDesc(String userId);
	
	@Modifying
	void deleteByUserId(String userId);

}
