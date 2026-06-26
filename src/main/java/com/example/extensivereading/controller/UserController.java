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
 * 登録画面の表示、ユーザー登録処理、および登録成功画面への遷移を担当する。
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
	 * @param model ビューへ空のフォームオブジェクトを渡すためのModelオブジェクト
	 * @return 新規登録画面のテンプレート名
	 */
	@GetMapping("/register")
	public String showRegisterForm(Model model) {
		model.addAttribute("userRegisterForm", new UserRegisterForm());
		return "register";
	}

	/**
	 * ユーザー登録処理を実行する。
	 * @param form ユーザー登録フォームに入力されたデータ
	 * @param result 入力値のバリデーション結果
	 * @param model ビューへエラーメッセージを渡すためのModelオブジェクト
	 * @param redirectAttributes リダイレクト先へ登録情報を渡すためのRedirectAttributes
	 * @return 処理結果に応じた画面またはリダイレクト先のテンプレート名
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
	 * 登録成功画面を表示する。
	 * @param model 登録情報の有無を確認するためのModelオブジェクト
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
