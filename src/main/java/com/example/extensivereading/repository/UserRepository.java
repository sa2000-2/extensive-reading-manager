package com.example.extensivereading.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.extensivereading.entity.User;

/**
 * ユーザー情報のデータベース操作を担当するRepositoryインターフェース。
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

}