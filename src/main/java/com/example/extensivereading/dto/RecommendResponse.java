package com.example.extensivereading.dto;

import lombok.Data;

@Data
public class RecommendResponse {
	private String title;
    private String author;
    private String publisher;
    private String summary;
}
