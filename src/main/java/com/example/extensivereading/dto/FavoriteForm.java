package com.example.extensivereading.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class FavoriteForm {
	@NotBlank
	@Size(max = 255)
	private String title;

	@NotBlank
	@Size(max = 255)
	private String author;

	@NotBlank
	@Size(max = 255)
	private String publisher;

	@NotBlank
	@Size(max = 2000)
	private String summary;
}