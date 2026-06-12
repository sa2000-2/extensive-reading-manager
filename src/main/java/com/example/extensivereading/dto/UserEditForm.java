package com.example.extensivereading.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class UserEditForm {
    @Size(max = 100, message = "表示名は100文字以下で入力してください")
    private String name;
    
    @Pattern(regexp ="^$|^[a-zA-Z0-9!@#$%^&*()_+=\\-]{8,64}$", message = "パスワードを変更する場合は、8文字以上64文字以下の半角英数字および一部の記号で入力してください")
    private String newPassword;
    
    @Size(max = 64)
    private String confirmNewPassword;
    
    @Min(value = 0, message = "目標語数は0語以上で入力してください")
    private Integer targetWords;
}