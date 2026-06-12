package com.example.extensivereading.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * データベースのUSERSテーブルと対応するEntityクラス。
 * データの保存や取得を行う。
 */

@Entity
@Table(name = "USERS") 
@Getter
@Setter
@NoArgsConstructor
public class User implements Serializable {

    @Id
    @Column(name = "ID", length = 36, nullable = false)
    private String id;

    @Column(name = "NAME", length = 100, nullable = false)
    private String name;

    @Column(name = "PASSWORD_HASH", length = 255, nullable = false)
    private String passwordHash; 

    @Column(name = "TARGET_WORDS", nullable = false)
    @Min(1)
    private int targetWords = 1000000;

    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;
    
    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}