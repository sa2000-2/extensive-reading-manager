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

import com.example.extensivereading.dto.FavoriteForm;
import com.example.extensivereading.dto.RecommendForm;
import com.example.extensivereading.dto.RecommendResponse;
import com.example.extensivereading.entity.Favorite;
import com.example.extensivereading.security.UserDetailsImpl;
import com.example.extensivereading.service.RecommendService;

/**
 * AIおすすめ本表示機能とお気に入り登録に関するリクエストを制御するControllerクラス。
 * 画面表示、AIおすすめ書籍の取得、お気に入りの登録・一覧表示・削除を行い適切な画面へ遷移させる。
 */
@Controller
@RequestMapping("/recommend")
public class RecommendController {

    private final RecommendService recommendService;

    public RecommendController(RecommendService recommendService) {
        this.recommendService = recommendService;
    }

    /**
     * AIおすすめ本表示画面を表示する。
     * @param model ビューへ入力フォームを渡すためのModelオブジェクト
     * @return AIおすすめ画面のテンプレート名
     */
    @GetMapping
    public String showRecommendForm(Model model) {
        model.addAttribute("recommendForm", new RecommendForm());
        return "recommend";
    }

    
    /**
     * 入力条件をもとにAIへおすすめ書籍の取得を依頼する。
     * @param form AIへ送信する検索条件
     * @param result 入力チェックの結果
     * @param model ビューへおすすめリストやエラーメッセージを渡すためのModelオブジェクト
     * @return AIおすすめ画面のテンプレート名
     */
    @PostMapping("/ask")
    public String askAI(@Validated RecommendForm form, 
            BindingResult result, 
            Model model) {
    	if (result.hasErrors()) {
    		return "recommend";
    		}

        try {
            List<RecommendResponse> aiRecommendations = recommendService.executeRecommend(
                    form.getLevel(), form.getType(), form.getGenre()
            );
            model.addAttribute("aiRecommendations", aiRecommendations);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "AIからの応答の取得に失敗しました。時間をおいて再度お試しください。");
        }

        return "recommend"; 
    }

    
    /**
     * おすすめ本をお気に入り登録する。
     * @param userDetails ログイン中のユーザー情報
     * @param favoriteForm お気に入り登録する書籍情報
     * @param result 入力値のバリデーション結果
     * @param redirectAttributes リダイレクト先へメッセージを渡すためのRedirectAttributes
     * @return  AIおすすめ画面またはお気に入り一覧画面へのリダイレクトするURL
     */
    @PostMapping("/favorite")
    public String addFavorite(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Validated FavoriteForm favoriteForm,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "お気に入りの保存に失敗しました。");
            return "redirect:/recommend";
        }

        recommendService.saveFavorite(
                userDetails.getUsername(),
                favoriteForm);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "お気に入りに保存しました。");

        return "redirect:/recommend/favorites";
    }

    
    /**
     * お気に入り一覧画面を表示する。
     * @param userDetails ログイン中のユーザー情報
     * @param model ビューへお気に入り一覧を画面へ渡すためのModelオブジェクト
     * @return お気に入り一覧画面のテンプレート名
     */
    @GetMapping("/favorites")
    public String showFavorites(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model) {
        List<Favorite> favoriteList = recommendService.getFavorites(userDetails.getUsername());
        model.addAttribute("favoriteList", favoriteList);
        return "favoriteList";
    }

    
    /**
     * お気に入り登録を削除する。
     * @param userDetails 認証済みのユーザー情報
     * @param id お気に入り登録ID
     * @param redirectAttributes リダイレクト先へメッセージを渡すためのRedirectAttributes
     * @return お気に入り一覧画面へリダイレクトするURL
     */
    @PostMapping("/favorite/delete/{id}")
    public String deleteFavorite(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                 @PathVariable Integer id,
                                 RedirectAttributes redirectAttributes) {
        
        try {
            recommendService.deleteFavorite(userDetails.getUsername(), id);
        } catch (IllegalArgumentException | AccessDeniedException e) {
            return "redirect:/recommend/favorites";
        }
        
        redirectAttributes.addFlashAttribute("successMessage", "リストから削除しました。");
        return "redirect:/recommend/favorites";
    }
}