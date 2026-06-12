package com.example.extensivereading.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.extensivereading.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

}