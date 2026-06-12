package com.example.extensivereading.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class BoardForm {
    
    private Integer postId;

    @NotBlank(message = "投稿を入力してください")
    @Size(max = 255, message = "投稿は255字以内で入力してください")
    private String text; 

}