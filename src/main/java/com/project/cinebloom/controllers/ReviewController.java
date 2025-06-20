package com.project.cinebloom.controllers;

import com.project.cinebloom.dtos.ReviewFormDTO;
import com.project.cinebloom.domain.User;
import com.project.cinebloom.repositories.security.UserRepository;
import com.project.cinebloom.services.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final UserRepository userRepo;

    @PostMapping("/movie/{movieId}")
    public String addReview(@PathVariable Long movieId,
                            @Valid @ModelAttribute("reviewForm") ReviewFormDTO form,
                            BindingResult result,
                            Principal principal) {

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

        return "redirect:/movies/" + movieId;
    }
}
