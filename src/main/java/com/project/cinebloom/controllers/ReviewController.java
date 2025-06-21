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
    private final UserRepository userRepo;
    private final ReviewRepository reviewRepo;
    private final MovieStatsRepository statsRepo;

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

        User user = userRepo.findByUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        try {
            reviewService.addReview(movieId, user, form);
        } catch (IllegalStateException e) {
            //thrown if user already reviewed the movie
            return "redirect:/movies/" + movieId + "?error=duplicate";
        }

        redirectAttributes.addFlashAttribute("reviewSuccess", "Review added successfully!");
        return "redirect:/movies/" + movieId;
    }

    @PostMapping("/edit")
    public String updateReview(@ModelAttribute("reviewForm") @Valid ReviewFormDTO form,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        Review review = reviewRepo.findById(form.getId())
                .orElseThrow(() -> new RuntimeException("Review not found"));

        if (!review.getUser().getUsername().equals(principal.getName())) {
            return "redirect:/access-denied";
        }

        review.setValue(form.getValue());
        review.setComment(form.getComment());

        reviewRepo.save(review);
        redirectAttributes.addFlashAttribute("reviewSuccess", "Review updated successfully!");

        return "redirect:/movies/" + review.getMovie().getId();
    }

    @PostMapping("/delete/{reviewId}")
    public String deleteReview(@PathVariable Long reviewId, Principal principal,
                               RedirectAttributes redirectAttributes) {
        Review review = reviewRepo.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        if (!review.getUser().getUsername().equals(principal.getName())) {
            return "redirect:/access-denied";
        }

        Long movieId = review.getMovie().getId();

        MovieStats stats = review.getMovie().getStats();
        stats.setTotalReviews(stats.getTotalReviews() - 1);
        statsRepo.save(stats);

        reviewRepo.deleteById(reviewId);
        redirectAttributes.addFlashAttribute("reviewSuccess", "Review deleted successfully!");
        return "redirect:/movies/" + movieId;
    }
}
