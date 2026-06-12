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
 * ログインに関するリクエストを制御するControllerクラス。
 * ログイン画面の表示(GET)およびログイン後のメイン画面表示(GET)を管理する。
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
     * *@return ログイン画面のテンプレート名 
     */
	@GetMapping("/login")
	public String login() {
		return "login";
	}
	
	/**
     * ログイン成功後のメイン画面を表示する。
     * * @param userDetails 認証済みのユーザー情報
     * @param model HTML表示用にデータを格納するモデル
     * @return メイン画面のテンプレート名
     * @throws IllegalStateException データベースに該当するユーザーが存在しない場合に発生
     */
	@GetMapping("/main")
    public String main(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model) {
        String userId = userDetails.getUsername();
        User user = userRepository.findById(userId)
        		.orElseThrow(() ->
        		new IllegalStateException("ユーザー情報が存在しません"));
        
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
