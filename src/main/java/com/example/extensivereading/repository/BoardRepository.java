package com.example.extensivereading.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import com.example.extensivereading.entity.Board;

public interface BoardRepository extends JpaRepository<Board, Integer>{
	
    List<Board> findAllByOrderByPostDateDesc();
    
    List<Board> findByUserId(String userId);
    
    @Modifying
    void deleteByUserId(String userId);


}
