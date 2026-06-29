package com.example.extensivereading.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import lombok.Data;

@Data
public class StoryForm {
	@NotBlank(message = "レベルを選択してください")
    @Pattern(regexp = "^[1-9]$", message = "レベルは1〜9で選択してください")
    private String level;
	
	@NotBlank(message = "語数を選択してください")
	private String wordCount;

    @NotBlank(message = "ジャンルを選択してください")
    private String genre;

}
