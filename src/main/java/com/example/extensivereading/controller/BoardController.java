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
 * 投稿記録に関するリクエストを制御するControllerクラス。
 * 画面表示(GET)と登録、変更、削除処理(POST)を管理し適切な画面へ誘導する。
 */
@Controller
@RequestMapping("/board")
public class BoardController {
	private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }
    

    /**
     * 投稿リストを表示する
     * @param model 全投稿リストと空の投稿フォーム箱を運ぶためのモデル箱
     * @return 掲示板画面のテンプレート名
     */
    @GetMapping("/list")
    public String showList(Model model) {
    	
        List<Board> boardList = boardService.getAllBoardList();
        model.addAttribute("boardList", boardList); 
        
        // 新規投稿用の空のDTO
        model.addAttribute("boardForm", new BoardForm());
        
        return "boardList";
    }


    /**
     * 投稿を登録する
     * @param userDetails ログイン中のユーザー情報
     * @param boardForm 入力された投稿フォーム箱
     * @param bindingResult 入力チェックの結果
     * @param model 全投稿リストを運ぶためのモデル箱
     * @param redirectAttributes リダイレクトの時にメッセージを運ぶための箱
     * @return 掲示板画面のテンプレート名、または掲示板画面にとぶためのURL
     */
    @PostMapping("/add")
    public String addBoard(@AuthenticationPrincipal UserDetailsImpl userDetails,
    	    @Validated BoardForm boardForm,
    	    BindingResult bindingResult,
    	    Model model,RedirectAttributes redirectAttributes) {

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
     * 投稿の変更画面を表示する
     * @param userDetails ログイン中のユーザー情報
     * @param postId 投稿ID
     * @param model HTMLにデータを運ぶためのモデル箱
     * @return 編集画面のテンプレート名、または掲示板画面にとぶためのURL
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
     * 投稿を変更する
     * @param userDetails ログイン中のユーザー情報
     * @param boardForm 入力された投稿フォーム箱
     * @param bindingResult 入力チェックの結果
     * @param model HTMLにデータを運ぶためのモデル箱
     * @param redirectAttributes リダイレクトの時にメッセージを運ぶための箱
     * @return 編集画面のテンプレート名、または掲示板画面にとぶためのURL
     */
    @PostMapping("/update")
    public String editBoard(@AuthenticationPrincipal UserDetailsImpl userDetails,
    	    @Validated BoardForm boardForm,
    	    BindingResult bindingResult,
    	    Model model,RedirectAttributes redirectAttributes) {

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
     * 投稿を削除する
     * @param userDetails ログイン中のユーザー情報
     * @param postId 投稿ID
     * @param redirectAttributes リダイレクトの時にメッセージを運ぶための箱
     * @return 掲示板画面にとぶためのURL
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
