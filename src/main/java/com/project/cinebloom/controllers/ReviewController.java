package com.project.cinebloom.controllers;

import com.project.cinebloom.domain.MovieStats;
import com.project.cinebloom.domain.Review;
import com.project.cinebloom.dtos.ReviewFormDTO;
import com.project.cinebloom.domain.User;
import com.project.cinebloom.repositories.MovieStatsRepository;
import com.project.cinebloom.repositories.ReviewRepository;
import com.project.cinebloom.repositories.security.UserRepository;
import com.project.cinebloom.services.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/movie/{movieId}")
    public String addReview(@PathVariable Long movieId,
                            @Valid @ModelAttribute("reviewForm") ReviewFormDTO form,
                            BindingResult result,
                            Principal principal,
                            RedirectAttributes redirectAttributes) {

        if (principal == null) {
            return "redirect:/login";
        }

        if (result.hasErrors()) {
            return "redirect:/movies/" + movieId + "?error=form";
        }

        try {
            reviewService.addReview(movieId, principal.getName(), form);
            redirectAttributes.addFlashAttribute("reviewSuccess", "Review added successfully!");
        } catch (IllegalStateException e) {
            return "redirect:/movies/" + movieId + "?error=duplicate";
        } catch (UsernameNotFoundException e) {
            return "redirect:/login?error=user";
        } catch (Exception e) {
            return "redirect:/movies/" + movieId + "?error=unexpected";
        }

        return "redirect:/movies/" + movieId;
    }

    @PostMapping("/edit")
    public String updateReview(@ModelAttribute("reviewForm") @Valid ReviewFormDTO form,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        try {
            reviewService.updateReview(form, principal.getName());
            redirectAttributes.addFlashAttribute("reviewSuccess", "Review updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("reviewError", e.getMessage());
        }

        return "redirect:/movies/" + form.getMovieId();
    }

    @PostMapping("/delete/{reviewId}")
    public String deleteReview(@PathVariable Long reviewId,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        try {
            Long movieId = reviewService.deleteReview(reviewId, principal.getName());
            redirectAttributes.addFlashAttribute("reviewSuccess", "Review deleted successfully!");
            return "redirect:/movies/" + movieId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("reviewError", e.getMessage());
        }

        return "redirect:/movies";
    }
}
