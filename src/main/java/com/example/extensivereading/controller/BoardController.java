package com.example.extensivereading.controller;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.extensivereading.dto.BoardForm;
import com.example.extensivereading.entity.Board;
import com.example.extensivereading.security.UserDetailsImpl;
import com.example.extensivereading.service.BoardService;

@Controller
@RequestMapping("/board")
public class BoardController {
	private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }
    

    @GetMapping("/list")
    public String showList(Model model) {
    	
        List<Board> boardList = boardService.getAllBoardList();
        model.addAttribute("boardList", boardList); 
        
        // 新規投稿用の空のDTO
        model.addAttribute("boardForm", new BoardForm());
        
        return "boardList";
    }


    @PostMapping("/add")
    public String addBoard(@AuthenticationPrincipal UserDetailsImpl userDetails,
    	    @Validated @ModelAttribute BoardForm boardForm,
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

    @PostMapping("/update")
    public String editBoard(@AuthenticationPrincipal UserDetailsImpl userDetails,
    	    @Validated @ModelAttribute BoardForm boardForm,
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
