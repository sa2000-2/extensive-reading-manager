package com.example.extensivereading.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
public class BookRecordForm {
    
    private Integer recordId;
    
    @NotBlank(message = "本のタイトルを入力してください")
    private String bookTitle; 

    @NotNull(message = "読んだ日を入力してください")
    @PastOrPresent(message = "未来の日付は入力できません")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate readDate;

    @NotNull(message = "語数を入力してください")
    @Min(value = 0, message = "語数は0以上で入力してください")
    private Integer wordCount;
}