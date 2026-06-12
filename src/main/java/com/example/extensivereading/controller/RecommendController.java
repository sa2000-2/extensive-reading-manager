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

import com.example.extensivereading.dto.FavoriteForm;
import com.example.extensivereading.dto.RecommendForm;
import com.example.extensivereading.dto.RecommendResponse;
import com.example.extensivereading.entity.Favorite;
import com.example.extensivereading.security.UserDetailsImpl;
import com.example.extensivereading.service.RecommendService;

@Controller
@RequestMapping("/recommend")
public class RecommendController {

    private final RecommendService recommendService;

    public RecommendController(RecommendService recommendService) {
        this.recommendService = recommendService;
    }

    @GetMapping
    public String showRecommendForm(Model model) {
        model.addAttribute("recommendForm", new RecommendForm());
        return "recommend";
    }

    @PostMapping("/ask")
    public String askAI(@Validated @ModelAttribute("recommendForm") RecommendForm form, 
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

    @PostMapping("/favorite")
    public String addFavorite(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Validated @ModelAttribute("favoriteForm") FavoriteForm favoriteForm,
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

    @GetMapping("/favorites")
    public String showFavorites(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model) {
        List<Favorite> favoriteList = recommendService.getFavorites(userDetails.getUsername());
        model.addAttribute("favoriteList", favoriteList);
        return "favoriteList";
    }

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