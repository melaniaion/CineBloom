package com.project.cinebloom.services;

import com.project.cinebloom.domain.Movie;
import com.project.cinebloom.domain.MovieStats;
import com.project.cinebloom.domain.Review;
import com.project.cinebloom.domain.User;
import com.project.cinebloom.dtos.ReviewFormDTO;
import com.project.cinebloom.repositories.MovieRepository;
import com.project.cinebloom.repositories.MovieStatsRepository;
import com.project.cinebloom.repositories.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final MovieRepository movieRepo;
    private final ReviewRepository reviewRepo;
    private final MovieStatsRepository statsRepo;

    @Override
    public void addReview(Long movieId, User user, ReviewFormDTO dto) {
        Movie movie = movieRepo.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found"));

        //Check if the user already reviewed this movie
        if (reviewRepo.findByUserAndMovie(user, movie).isPresent()) {
            throw new IllegalStateException("You have already reviewed this movie.");
        }

        //Create and save the review
        Review review = Review.builder()
                .user(user)
                .movie(movie)
                .value(dto.getValue())
                .comment(dto.getComment())
                .createdAt(LocalDateTime.now())
                .build();

        reviewRepo.save(review);

        //Update movie stats
        MovieStats stats = movie.getStats();
        stats.setTotalReviews(stats.getTotalReviews() + 1);
        statsRepo.save(stats);
    }
}
