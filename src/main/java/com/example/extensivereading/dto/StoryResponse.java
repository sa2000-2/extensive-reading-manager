package com.example.extensivereading.dto;

import lombok.Data;

@Data
public class StoryResponse {
	private String title;
	private String contents;
	private String wordCount;
	private String japanese;
}
