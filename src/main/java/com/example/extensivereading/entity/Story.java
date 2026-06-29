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
@Table(name = "story")
@Getter
@Setter
@NoArgsConstructor
public class Story {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer storyId;

	@Column(nullable = false)
	private String userId;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String contents;

	@Column(nullable = false)
	private String wordCount;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String japanese;

	@Column(nullable = false)
	private LocalDateTime savedDate;

}
