package com.example.extensivereading.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.extensivereading.dto.UserRegisterForm;
import com.example.extensivereading.service.UserService;

/**
 * ユーザー登録に関するリクエストを制御するControllerクラス。
 * 画面表示(GET)と登録処理(POST)を管理し適切な画面へ誘導する。
 */
@Controller
@RequestMapping("/users")
public class UserController {
	private final UserService userService;
	
	public UserController(UserService userService) {
		this.userService = userService;
	}
	
	/**
     * 新規登録画面を表示する。
     * * @param model 空のフォームが入ったモデル
     * @return 新規登録画面のテンプレート名
     */
	@GetMapping("/register")
	public String showRegisterForm(Model model) {
        model.addAttribute("userRegisterForm", new UserRegisterForm());
        return "register"; 
    }
	
	
	/**
     * ユーザー登録処理を実行
     * * @param form 入力チェック済みのユーザー登録フォームのデータ
     * @param result バリデーション結果
     * @param model エラーメッセージなどをいれるモデル
     * @param redirectAttributes 登録成功画面にデータを渡すためのフラッシュスコープ
     * @return 処理結果に応じた画面へのリダイレクトパス
     * @throws IllegalArgumentException ID重複時に発生
     */
	@PostMapping("/register")
    public String register(@Validated @ModelAttribute("userRegisterForm") UserRegisterForm form,
                           BindingResult result,
                           Model model,
                           RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "register"; 
        }

        try {
            userService.register(form);
            redirectAttributes.addFlashAttribute("registeredName", form.getName());
            redirectAttributes.addFlashAttribute("registeredId", form.getId());
            
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMsg", e.getMessage());
            return "register";
        }

        return "redirect:/users/register/success";
        }
	
	/**
     * 登録成功画面を表示
     * * @param　登録されたIDと名前が入ってるmodel箱
     * @return 登録成功画面のテンプレート名
     */
    @GetMapping("/register/success")
    public String showRegisterSuccess(Model model) {
        
      
        if (!model.containsAttribute("registeredName")) {
            return "redirect:/users/register";
        }

        return "registerSuccess";
    }
}
