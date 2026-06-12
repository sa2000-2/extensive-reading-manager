package com.example.extensivereading.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "favorite")
@Getter
@Setter
@NoArgsConstructor
public class Favorite {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String bookTitle;

    @Column(nullable = false)
    private String author;
    
    @Column(nullable = false)
    private String publisher;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String summary;
    
    @Column(nullable = false)
    private LocalDateTime savedDate;

}
