package com.example.extensivereading.entity;

import java.time.LocalDate;

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
@Table(name = "bookrecords")
@Getter
@Setter
@NoArgsConstructor
public class BookRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer recordId;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private LocalDate readDate;

    @Column(nullable = false)
    private String bookTitle;

    @Column(nullable = false)
    private Integer wordCount;
}