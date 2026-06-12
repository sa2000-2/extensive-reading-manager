package com.example.extensivereading.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.extensivereading.dto.UserEditForm;
import com.example.extensivereading.entity.User;
import com.example.extensivereading.repository.UserRepository;
import com.example.extensivereading.security.UserDetailsImpl;
import com.example.extensivereading.service.UserEditService;

/**
 * ユーザー情報の編集および削除リクエストを制御するControllerクラス。
 */
@Controller
@RequestMapping("/users")
public class UserEditController {

    private final UserEditService userEditService;
    private final UserRepository userRepository;

    public UserEditController(UserEditService userEditService, UserRepository userRepository) {
        this.userEditService = userEditService;
        this.userRepository = userRepository;
    }

    
    /**
     * ユーザー情報変更画面を表示する。
     * @param userDetails 認証済みのユーザー情報
     * @param model フォームを格納するモデル
     * @return ユーザー情報変更画面のテンプレート名
     * @throws IllegalStateException データベースに該当するユーザー情報が存在しない場合
     */
    @GetMapping("/edit")
    public String showEditForm(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model) {
        User user = userRepository.findById(userDetails.getUsername())
        		.orElseThrow(()->
                new IllegalStateException("ユーザー情報が存在しません"));

        if (!model.containsAttribute("userEditForm")) {
            UserEditForm form = new UserEditForm();
            form.setName(user.getName());
            form.setTargetWords(user.getTargetWords());
            model.addAttribute("userEditForm", form);
        }
        return "userEdit"; 
    }
    
    
    /**
     * 削除確認画面を表示する。
     * @return 削除確認画面のテンプレート名
     */
    @GetMapping("/deleteConfirm") 
    public String showDeleteConfirm() {
        return "deleteConfirm";
    }

    
    /**
     * ユーザー情報の更新処理を実行する。
     * @param userDetails 認証済みのユーザー情報
     * @param form 入力チェック済みのユーザー編集フォーム
     * @param bindingResult 入力チェックの結果
     * @param model エラーメッセージ格納用のモデル
     * @param redirectAttributes リダイレクト時にメッセージを受け渡すための箱
     * @return 処理結果に応じた画面またはリダイレクト先のテンプレート名
     */
    @PostMapping("/edit")
    public String editUser(@AuthenticationPrincipal UserDetailsImpl userDetails,
                           @Validated UserEditForm form,
                           BindingResult bindingResult,
                           Model model,
                           RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "userEdit";
        }

        try {
            userEditService.updateUser(userDetails.getUsername(), form);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "userEdit";
        }

        redirectAttributes.addFlashAttribute("successMessage", "ユーザー情報を変更しました。");
        return "redirect:/users/edit";
    }
    
    
    /**
     * ユーザーアカウントの削除処理を実行する。
     * @param userDetails 認証済みのユーザー情報
     * @param request セッション破棄のためのサーブレットリクエスト
     * @param redirectAttributes エラー時のメッセージ用箱
     * @return 処理結果に応じた画面またはリダイレクト先のテンプレート名
     * @throws ServletException ログアウト処理失敗時に発生
     */
    @PostMapping("/delete")
    public String deleteUser(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes)
            throws ServletException {

        try {
            userEditService.deleteUser(userDetails.getUsername());
            request.logout();
            return "deleteSuccess";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                "errorMessage",
                "アカウント削除に失敗しました。");

            return "redirect:/users/edit";
        }
    }
    
}