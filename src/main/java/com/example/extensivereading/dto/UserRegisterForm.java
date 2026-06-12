package com.example.extensivereading.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.Data; // @Getter, @Setterなどを一括で付与するLombokのアノテーション

/**
 * 新規登録画面で入力されたデータを受け取り入力チェックを行うDTO
 * 画面からControllerへデータを運ぶための箱としての役割を持つ
 */
@Data
public class UserRegisterForm {

    @NotBlank(message = "IDを入力してください")
    @Size(min = 4, max = 36, message = "IDは4文字以上、36文字以下で入力してください")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "IDは半角英数字のみが使用できます")
    private String id;

    @NotBlank(message = "表示名を入力してください")
    @Size(max = 100, message = "表示名は100文字以下で入力してください")
    private String name;

    @NotBlank(message = "パスワードを入力してください")
    @Size(min = 8, max = 64, message = "パスワードは8文字以上、64文字以下で入力してください")
    @Pattern(regexp = "^[a-zA-Z0-9!@#$%^&*()_+=\\-]+$", message = "パスワードには半角英数字および一部の記号のみ使用できます")
    private String password;
}
