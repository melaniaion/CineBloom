package com.project.cinebloom.controllers;

import com.project.cinebloom.domain.User;
import com.project.cinebloom.domain.WatchStatus;
import com.project.cinebloom.dtos.MovieSummaryDTO;
import com.project.cinebloom.services.UserMovieService;
import com.project.cinebloom.services.security.UserService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@AllArgsConstructor
public class WatchListController {
    UserService userService;
    UserMovieService userMovieService;

    @GetMapping("/my-movies")
    public String myMoviesHome() {
        return "myMoviesHome";
    }

    @PostMapping("/watchlist")
    public String addToWatchlist(@RequestParam Long movieId,
                                 @RequestParam WatchStatus status,
                                 Principal principal) {
        if (principal == null) return "redirect:/login";

        try {
            userMovieService.addToWatchList(principal.getName(), movieId, status);
        } catch (Exception e) {
            return "redirect:/movies/" + movieId + "?error=unexpected";
        }

        return "redirect:/movies/" + movieId;
    }

    @GetMapping("/my-movies/{status}")
    public String viewUserMovieList(@PathVariable WatchStatus status,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "desc") String sort,
                                    Principal principal,
                                    Model model) {

        User user = userService.findByUsername(principal.getName())
                .orElse(null);

        Page<MovieSummaryDTO> moviePage = userMovieService.getUserMoviesByStatus(user, status, page, sort);

        model.addAttribute("movies", moviePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", moviePage.getTotalPages());
        model.addAttribute("totalItems", moviePage.getTotalElements());
        model.addAttribute("hasPrevious", moviePage.hasPrevious());
        model.addAttribute("hasNext", moviePage.hasNext());

        model.addAttribute("selectedStatus", status);
        model.addAttribute("sortOrder", sort);

        return "myWatchList";
    }

    @PostMapping("/my-movies/remove")
    public String removeMovieFromList(@RequestParam Long movieId,
                                      @RequestParam WatchStatus status,
                                      Principal principal,
                                      RedirectAttributes redirectAttributes) {

        User user = userService.findByUsername(principal.getName())
                .orElse(null);

        userMovieService.removeMovieFromList(user, movieId, status);

        redirectAttributes.addFlashAttribute("successMessage", "Movie removed from list.");
        return "redirect:/my-movies/" + status;
    }

    @PostMapping("/my-movies/move")
    public String moveMovieToOtherList(@RequestParam Long movieId,
                                       @RequestParam WatchStatus newStatus,
                                       Principal principal,
                                       RedirectAttributes redirectAttributes) {

        User user = userService.findByUsername(principal.getName())
                .orElse(null);

        try {
            userMovieService.moveMovieToList(user, movieId, newStatus);
            redirectAttributes.addFlashAttribute("successMessage", "Movie moved to " + newStatus.getDescription() + ".");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        redirectAttributes.addFlashAttribute("successMessage", "Movie moved to " + newStatus.getDescription() + ".");
        return "redirect:/my-movies/" + newStatus;
    }

}
