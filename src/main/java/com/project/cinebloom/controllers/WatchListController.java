package com.project.cinebloom.controllers;

import com.project.cinebloom.domain.*;
import com.project.cinebloom.repositories.MovieRepository;
import com.project.cinebloom.repositories.MovieStatsRepository;
import com.project.cinebloom.repositories.UserMovieRepository;
import com.project.cinebloom.repositories.security.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/watchlist")
public class WatchListController {

    private final UserRepository userRepo;
    private final MovieRepository movieRepo;
    private final UserMovieRepository userMovieRepo;
    private final MovieStatsRepository statsRepo;

    @PostMapping
    public String addToWatchlist(@RequestParam Long movieId,
                                 @RequestParam WatchStatus status,
                                 Principal principal) {

        if (principal == null) return "redirect:/login";

        User user = userRepo.findByUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Movie movie = movieRepo.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found"));

        boolean alreadyExists = userMovieRepo.findByUserAndMovie(user, movie).isPresent();

        if (!alreadyExists) {
            UserMovie userMovie = UserMovie.builder()
                    .user(user)
                    .movie(movie)
                    .status(status)
                    .favorite(true)
                    .build();

            userMovieRepo.save(userMovie);

            MovieStats stats = movie.getStats();
            stats.setTotalFavorites(stats.getTotalFavorites() + 1);
            statsRepo.save(stats);
        }

        return "redirect:/movies/" + movieId;
    }
}
