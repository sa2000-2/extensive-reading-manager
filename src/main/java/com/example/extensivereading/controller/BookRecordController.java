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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.extensivereading.dto.BookRecordForm;
import com.example.extensivereading.entity.BookRecord;
import com.example.extensivereading.security.UserDetailsImpl;
import com.example.extensivereading.service.BookRecordService;

/**
 * 読書記録に関するリクエストを制御するControllerクラス。
 * 一覧・登録・編集画面の表示、および読書記録の登録・更新・削除を担当する。
 */
@Controller
@RequestMapping("/books")
public class BookRecordController {
	private final BookRecordService bookRecordService;

	public BookRecordController(BookRecordService bookRecordService) {
		this.bookRecordService = bookRecordService;
	}

	/**
	 * 読書記録のリストを表示する。
	 * @param userDetails 認証済みのユーザー情報
	 * @param model ビューへ読書記録一覧を渡すためのModelオブジェクト
	 * @return 読書記録のリスト画面のテンプレート名
	 */
	@GetMapping("/list")
	public String showList(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model) {
		String userId = userDetails.getUsername();
		int targetWords = bookRecordService.getTargetWords(userId);

		List<BookRecord> records = bookRecordService.getAllRecords(userId);
		int totalWords = bookRecordService.calculateTotalWords(records);
		double progress = bookRecordService.calculateProgressPercentage(totalWords, targetWords);

		model.addAttribute("records", records);
		model.addAttribute("totalWords", totalWords);
		model.addAttribute("targetWords", targetWords);
		model.addAttribute("progress", progress);

		return "bookRecordList";
	}

	/**
	 * 読書記録の登録画面を表示する。
	 * @param model ビューへ空のフォームオブジェクトを渡すためのModelオブジェクト
	 * @return 読書記録登録画面のテンプレート名
	 */
	@GetMapping("/add")
	public String showAddForm(Model model) {
		model.addAttribute("bookRecordForm", new BookRecordForm());
		return "bookRecordAdd";
	}

	/**
	 * 読書記録を登録する。
	 * @param userDetails 認証済みのユーザー情報
	 * @param bookRecordForm 読書記録登録フォームに入力されたデータ
	 * @param bindingResult 入力値のバリデーション結果
	 * @param redirectAttributes リダイレクト先へ成功メッセージを渡すためのRedirectAttributes
	 * @return 読書記録登録画面にリダイレクトするURL
	 */
	@PostMapping("/add")
	public String addRecord(@AuthenticationPrincipal UserDetailsImpl userDetails,
			@Validated BookRecordForm bookRecordForm,
			BindingResult bindingResult,
			RedirectAttributes redirectAttributes) {

		if (bindingResult.hasErrors()) {
			return "bookRecordAdd";
		}

		bookRecordService.bookRegister(userDetails.getUsername(), bookRecordForm);

		redirectAttributes.addFlashAttribute("successMessage", "読書記録を登録しました。続けて別の本を登録できます。");
		return "redirect:/books/add"; //

	}

	/**
	 * 読書記録編集画面を表示する。
	 * @param userDetails 認証済みのユーザー情報
	 * @param recordId 編集対象の読書記録ID
	 * @param model ビューへ編集対象の読書記録を渡すためのModelオブジェクト
	 * @return 読書記録変更画面のテンプレート名、またはエラー時にリスト画面へリダイレクトするURL
	 */
	@GetMapping("/edit/{recordId}")
	public String showEditForm(@AuthenticationPrincipal UserDetailsImpl userDetails,
			@PathVariable Integer recordId,
			Model model) {

		try {
			BookRecord record = bookRecordService.getRecord(userDetails.getUsername(), recordId);

			BookRecordForm form = new BookRecordForm();
			form.setRecordId(record.getRecordId());
			form.setBookTitle(record.getBookTitle());
			form.setReadDate(record.getReadDate());
			form.setWordCount(record.getWordCount());

			model.addAttribute("bookRecordForm", form);
			return "bookRecordEdit";
		} catch (IllegalArgumentException | AccessDeniedException e) {
			return "redirect:/books/list";
		}
	}

	/**
	 * 読書記録を編集する。
	 * @param userDetails 認証済みのユーザー情報
	 * @param bookRecordForm 編集フォームに入力された読書記録の情報
	 * @param bindingResult 入力値のバリデーション結果
	 * @param redirectAttributes リダイレクト先へメッセージを渡すためのRedirectAttributes
	 * @return 読書記録リスト画面にリダイレクトするURL
	 */
	@PostMapping("/update")
	public String editRecord(@AuthenticationPrincipal UserDetailsImpl userDetails,
			@Validated BookRecordForm bookRecordForm,
			BindingResult bindingResult,
			RedirectAttributes redirectAttributes) {

		if (bindingResult.hasErrors()) {
			return "bookRecordEdit";
		}

		try {
			bookRecordService.updateRecord(userDetails.getUsername(), bookRecordForm);
		} catch (IllegalArgumentException | AccessDeniedException e) {
			return "redirect:/books/list";
		}

		redirectAttributes.addFlashAttribute("successMessage", "読書記録を更新しました。");
		return "redirect:/books/list";
	}

	/**
	 * 読書記録の削除をする。
	 * @param userDetails 認証済みのユーザー情報
	 * @param recordId 削除対象の読書記録ID
	 * @param redirectAttributes リダイレクト先へメッセージを渡すためのRedirectAttributes
	 * @return 読書記録リスト画面にリダイレクトするURL
	 */
	@PostMapping("/delete/{recordId}")
	public String deleteRecord(@AuthenticationPrincipal UserDetailsImpl userDetails,
			@PathVariable Integer recordId,
			RedirectAttributes redirectAttributes) {

		try {
			bookRecordService.deleteRecord(userDetails.getUsername(), recordId);
		} catch (IllegalArgumentException | AccessDeniedException e) {
			return "redirect:/books/list";
		}

		redirectAttributes.addFlashAttribute("successMessage", "読書記録を削除しました。");
		return "redirect:/books/list";
	}
}
