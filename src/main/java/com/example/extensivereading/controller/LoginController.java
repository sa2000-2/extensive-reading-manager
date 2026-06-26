package com.example.extensivereading.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.extensivereading.entity.BookRecord;
import com.example.extensivereading.entity.User;
import com.example.extensivereading.repository.UserRepository;
import com.example.extensivereading.security.UserDetailsImpl;
import com.example.extensivereading.service.BookRecordService;

/**
 * ログイン画面およびログイン後のメイン画面表示を担当するControllerクラス。
 */
@Controller
@RequestMapping("/users")
public class LoginController {
	private final BookRecordService bookRecordService;
	private final UserRepository userRepository;

	public LoginController(BookRecordService bookRecordService, UserRepository userRepository) {
		this.bookRecordService = bookRecordService;
		this.userRepository = userRepository;
	}

	/**
	 * ログイン画面を表示する。
	 * @return ログイン画面のテンプレート名 
	 */
	@GetMapping("/login")
	public String login() {
		return "login";
	}

	/**
	 * ログイン成功後のメイン画面を表示する。
	 * @param userDetails 認証済みのユーザー情報
	 * @param model ビューへ表示データを渡すためのModelオブジェクト
	 * @return メイン画面のテンプレート名
	 * @throws IllegalStateException 認証済みユーザーの情報がデータベースに存在しない場合
	 */
	@GetMapping("/main")
	public String main(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model) {
		String userId = userDetails.getUsername();
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalStateException("ユーザー情報が存在しません"));

		int targetWords = user.getTargetWords();

		List<BookRecord> records = bookRecordService.getAllRecords(userId);
		int totalWords = bookRecordService.calculateTotalWords(records);
		double progress = bookRecordService.calculateProgressPercentage(totalWords, targetWords);

		model.addAttribute("totalWords", totalWords);
		model.addAttribute("targetWords", targetWords);
		model.addAttribute("progress", progress);

		return "main";
	}

}
