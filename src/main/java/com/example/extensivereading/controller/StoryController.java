package com.example.extensivereading.controller;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.extensivereading.dto.StoryForm;
import com.example.extensivereading.dto.StoryResponse;
import com.example.extensivereading.entity.Story;
import com.example.extensivereading.security.UserDetailsImpl;
import com.example.extensivereading.service.StoryService;

/**
 * AIストーリー生成機能に関するリクエストを制御するControllerクラス。
 * ストーリー生成、読書記録への登録、ストーリーの保存・一覧表示・詳細表示・削除を行い、適切な画面へ遷移させる。
 */
@Controller
@RequestMapping("/story")
public class StoryController {
	private final StoryService storyService;

	public StoryController(StoryService storyService) {
		this.storyService = storyService;
	}

	/**
	 * AIストーリー画面を表示する。
	 * @param model ビューへ入力フォームを渡すためのModelオブジェクト
	 * @return AIストーリー画面のテンプレート名
	 */
	@GetMapping
	public String showStoryForm(Model model) {
		model.addAttribute("storyForm", new StoryForm());
		return "story";
	}

	/**
	 * 入力条件をもとにAIへストーリーの生成を依頼する。
	 * @param form AIへ送信する検索条件
	 * @param result 入力チェックの結果
	 * @param model ビューへストーリーやエラーメッセージを渡すためのModelオブジェクト
	 * @return AIストーリー画面のテンプレート名
	 */
	@PostMapping("/ask")
	public String askAI(@Validated StoryForm form,
			BindingResult result,
			Model model) {
		if (result.hasErrors()) {
			return "story";
		}

		try {
			StoryResponse aiStory = storyService.executeGenerate(
					form.getLevel(), form.getWordCount(), form.getGenre());

			model.addAttribute("aiStory", aiStory);
		} catch (Exception e) {
			model.addAttribute("errorMessage", "AIからの応答の取得に失敗しました。時間をおいて再度お試しください。");
		}

		return "story";
	}

	/**
	 * AIストーリー画面で生成したストーリーを読書記録へ登録する。
	 * @param userDetails ログイン中のユーザー情報
	 * @param storyResponse 登録するストーリー情報
	 * @param bindingResult 入力値のバリデーション結果
	 * @param redirectAttributes リダイレクト先へメッセージを渡すためのRedirectAttributes
	 * @return  AIストーリー画面または保存一覧画面へのリダイレクトするURL
	 */
	@PostMapping("/register")
	public String addRecord(@AuthenticationPrincipal UserDetailsImpl userDetails,
			@Validated StoryResponse storyResponse,
			BindingResult bindingResult,
			Model model) {

		if (bindingResult.hasErrors()) {
			return "story";
		}

		storyService.registerRead(userDetails.getUsername(), storyResponse);

		model.addAttribute("storyForm", new StoryForm());
		model.addAttribute("aiStory", storyResponse);

		model.addAttribute("successMessage", "読書記録を登録しました。");

		return "story";
	}

	/**
	 * 保存済みストーリー詳細画面から読書記録へ登録する。
	 * @param userDetails ログイン中のユーザー情報
	 * @param storyId 保存済みストーリーID
	 * @param storyResponse 登録するストーリー情報
	 * @param bindingResult 入力値のバリデーション結果
	 * @param model ビューへ画面表示に必要なデータを渡すためのModelオブジェクト
	 * @return ストーリー詳細画面のテンプレート名
	 */
	@PostMapping("/registerFromDetail")
	public String addRecordFromDetail(@AuthenticationPrincipal UserDetailsImpl userDetails,
			@RequestParam("storyId") Integer storyId,
			@Validated StoryResponse storyResponse,
			BindingResult bindingResult,
			Model model) {

		if (bindingResult.hasErrors()) {
			model.addAttribute("story", storyResponse);
			return "storyDetail";
		}

		storyService.registerRead(userDetails.getUsername(), storyResponse);

		model.addAttribute("story", storyResponse);
		model.addAttribute("storyId", storyId);
		model.addAttribute("successMessage", "読書記録を登録しました。");

		return "storyDetail";
	}

	/**
	 * ストーリーを保存する。
	 * @param userDetails ログイン中のユーザー情報
	 * @param storyResponse 保存するストーリー情報
	 * @param bindingResult 入力値のバリデーション結果
	 * @param redirectAttributes リダイレクト先へメッセージを渡すためのRedirectAttributes
	 * @return  AIストーリー画面または保存一覧画面へリダイレクトするURL
	 */
	@PostMapping("/saveStory")
	public String saveStory(
			@AuthenticationPrincipal UserDetailsImpl userDetails,
			@Validated StoryResponse storyResponse,
			BindingResult bindingResult,
			RedirectAttributes redirectAttributes) {

		if (bindingResult.hasErrors()) {
			redirectAttributes.addFlashAttribute(
					"errorMessage",
					"ストーリーの保存に失敗しました。");
			return "redirect:/story";
		}

		storyService.saveStory(
				userDetails.getUsername(),
				storyResponse);

		redirectAttributes.addFlashAttribute(
				"successMessage",
				"ストーリーを保存しました。");

		return "redirect:/story/savedStoryList";
	}

	/**
	 * ストーリー一覧画面を表示する。
	 * @param userDetails ログイン中のユーザー情報
	 * @param model ビューへストーリー一覧を渡すためのModelオブジェクト
	 * @return ストーリー保存一覧画面のテンプレート名
	 */
	@GetMapping("/savedStoryList")
	public String showStories(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model) {
		List<Story> storyList = storyService.getStory(userDetails.getUsername());
		model.addAttribute("storyList", storyList);
		return "savedStoryList";
	}

	/**
	 * 保存したストーリー1つの詳細画面を表示する。
	 * @param userDetails ログイン中のユーザー情報
	 * @param id ストーリーID
	 * @param model ビューへストーリーを渡すためのModelオブジェクト
	 * @return ストーリー詳細画面のテンプレート名
	 */
	@GetMapping("/savedStory/{id}")
	public String showStoryDetail(@AuthenticationPrincipal UserDetailsImpl userDetails,
			@PathVariable Integer id,
			Model model) {

		Story story = storyService.getStoryById(userDetails.getUsername(), id);

		model.addAttribute("story", story);
		return "storyDetail";
	}

	/**
	 * 保存したストーリーを削除する。
	 * @param userDetails 認証済みのユーザー情報
	 * @param id ストーリーID
	 * @param redirectAttributes リダイレクト先へメッセージを渡すためのRedirectAttributes
	 * @return ストーリー保存一覧画面へリダイレクトするURL
	 */
	@PostMapping("/delete/{id}")
	public String deleteStory(@AuthenticationPrincipal UserDetailsImpl userDetails,
			@PathVariable Integer id,
			RedirectAttributes redirectAttributes) {

		try {
			storyService.deleteStory(userDetails.getUsername(), id);
		} catch (IllegalArgumentException | AccessDeniedException e) {
			return "redirect:/story/savedStoryList";
		}

		redirectAttributes.addFlashAttribute("successMessage", "リストから削除しました。");
		return "redirect:/story/savedStoryList";
	}

}
