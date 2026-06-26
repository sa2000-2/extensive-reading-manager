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

import com.example.extensivereading.dto.BoardForm;
import com.example.extensivereading.entity.Board;
import com.example.extensivereading.security.UserDetailsImpl;
import com.example.extensivereading.service.BoardService;

/**
 * 掲示板投稿に関するリクエストを処理するControllerクラス。
 * 投稿一覧・編集画面の表示、および投稿の登録・更新・削除を担当する。
 */
@Controller
@RequestMapping("/board")
public class BoardController {
	private final BoardService boardService;

	public BoardController(BoardService boardService) {
		this.boardService = boardService;
	}

	/**
	 * 投稿リストを表示する。
	 * @param model ビューへ投稿一覧と空の投稿フォームを渡すためのModelオブジェクト
	 * @return 掲示板画面のテンプレート名
	 */
	@GetMapping("/list")
	public String showList(Model model) {

		List<Board> boardList = boardService.getAllBoardList();
		model.addAttribute("boardList", boardList);
		model.addAttribute("boardForm", new BoardForm());

		return "boardList";
	}

	/**
	 * 投稿を登録する。
	 * @param userDetails 認証済みのユーザー情報
	 * @param boardForm 投稿フォームに入力されたデータ
	 * @param bindingResult 入力値のバリデーション結果
	 * @param model ビューへ投稿一覧を渡すためのModelオブジェクト
	 * @param redirectAttributes リダイレクト先へメッセージを渡すためのRedirectAttributes
	 * @return 掲示板画面のテンプレート名、または掲示板画面へリダイレクトするURL
	 */
	@PostMapping("/add")
	public String addBoard(@AuthenticationPrincipal UserDetailsImpl userDetails,
			@Validated BoardForm boardForm,
			BindingResult bindingResult,
			Model model, RedirectAttributes redirectAttributes) {

		if (bindingResult.hasErrors()) {
			List<Board> boardList = boardService.getAllBoardList();
			model.addAttribute("boardList", boardList);
			return "boardList";
		}

		boardService.boardRegister(userDetails.getUsername(), boardForm);

		redirectAttributes.addFlashAttribute("successMessage", "投稿しました。");
		return "redirect:/board/list";

	}

	/**
	 * 投稿の変更画面を表示する。
	 * @param userDetails 認証済みのユーザー情報
	 * @param postId 編集対象の投稿ID
	 * @param model ビューへ編集対象の投稿を渡すためのModelオブジェクト
	 * @return 投稿編集画面のテンプレート名、または掲示板画面へリダイレクトするURL
	 */
	@GetMapping("/edit/{postId}")
	public String showEditForm(@AuthenticationPrincipal UserDetailsImpl userDetails,
			@PathVariable Integer postId,
			Model model) {
		try {
			Board board = boardService.getBoard(userDetails.getUsername(), postId);

			BoardForm form = new BoardForm();
			form.setPostId(board.getPostId());
			form.setText(board.getText());

			model.addAttribute("boardForm", form);
			return "boardEdit";

		} catch (IllegalArgumentException | AccessDeniedException e) {
			return "redirect:/board/list";
		}
	}

	/**
	 * 投稿を変更する。
	 * @param userDetails 認証済みのユーザー情報
	 * @param boardForm 編集フォームに入力された投稿データ
	 * @param bindingResult 入力値のバリデーション結果
	 * @param model ビューへ投稿情報を渡すためのModelオブジェクト
	 * @param redirectAttributes リダイレクト先へメッセージを渡すためのRedirectAttributes
	 * @return 投稿編集画面のテンプレート名、または掲示板画面へリダイレクトするURL
	 */
	@PostMapping("/update")
	public String editBoard(@AuthenticationPrincipal UserDetailsImpl userDetails,
			@Validated BoardForm boardForm,
			BindingResult bindingResult,
			Model model, RedirectAttributes redirectAttributes) {

		if (bindingResult.hasErrors()) {
			model.addAttribute("boardForm", boardForm);
			return "boardEdit";
		}

		try {
			boardService.updateBoard(userDetails.getUsername(), boardForm);
		} catch (IllegalArgumentException | AccessDeniedException e) {
			return "redirect:/board/list";
		}

		redirectAttributes.addFlashAttribute("successMessage", "投稿を更新しました。");
		return "redirect:/board/list";
	}

	/**
	 * 投稿を削除する。
	 * @param userDetails 認証済みのユーザー情報
	 * @param postId 削除対象の投稿ID
	 * @param redirectAttributes リダイレクト先へメッセージを渡すためのRedirectAttributes
	 * @return 掲示板画面へリダイレクトするURL
	 */
	@PostMapping("/delete/{postId}")
	public String deleteBoard(@AuthenticationPrincipal UserDetailsImpl userDetails,
			@PathVariable Integer postId,
			RedirectAttributes redirectAttributes) {
		try {
			boardService.deleteBoard(userDetails.getUsername(), postId);
		} catch (IllegalArgumentException | AccessDeniedException e) {
			return "redirect:/board/list";
		}

		redirectAttributes.addFlashAttribute("successMessage", "投稿を削除しました。");
		return "redirect:/board/list";
	}
}
