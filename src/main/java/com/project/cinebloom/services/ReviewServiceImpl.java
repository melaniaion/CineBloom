package com.project.cinebloom.services;

import com.project.cinebloom.domain.Movie;
import com.project.cinebloom.domain.MovieStats;
import com.project.cinebloom.domain.Review;
import com.project.cinebloom.domain.User;
import com.project.cinebloom.dtos.ReviewFormDTO;
import com.project.cinebloom.exceptions.MovieNotFoundException;
import com.project.cinebloom.exceptions.ReviewNotFoundException;
import com.project.cinebloom.repositories.MovieRepository;
import com.project.cinebloom.repositories.MovieStatsRepository;
import com.project.cinebloom.repositories.ReviewRepository;
import com.project.cinebloom.repositories.security.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final MovieRepository movieRepo;
    private final ReviewRepository reviewRepo;
    private final MovieStatsRepository statsRepo;
    private final UserRepository userRepo;

    @Override
    public void addReview(Long movieId, String username, ReviewFormDTO form) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Movie movie = movieRepo.findById(movieId)
                .orElseThrow(() -> new MovieNotFoundException(movieId));

        if (reviewRepo.existsByUserAndMovie(user, movie)) {
            throw new IllegalStateException("User has already reviewed this movie");
        }

        Review review = Review.builder()
                .user(user)
                .movie(movie)
                .value(form.getValue())
                .comment(form.getComment())
                .createdAt(LocalDateTime.now())
                .build();

        reviewRepo.save(review);

        MovieStats stats = movie.getStats();
        stats.setTotalReviews(stats.getTotalReviews() + 1);
        statsRepo.save(stats);
    }


    @Override
    public void updateReview(ReviewFormDTO form, String username) {
        Review review = reviewRepo.findById(form.getId())
                .orElseThrow(() -> new ReviewNotFoundException(form.getId()));

        if (!review.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("User not authorized");
        }

        review.setValue(form.getValue());
        review.setComment(form.getComment());

        reviewRepo.save(review);
    }

    @Override
    public Long deleteReview(Long reviewId, String username) {
        Review review = reviewRepo.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));

        if (!review.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("User not authorized");
        }

        Long movieId = review.getMovie().getId();

        MovieStats stats = review.getMovie().getStats();
        stats.setTotalReviews(stats.getTotalReviews() - 1);
        statsRepo.save(stats);

        reviewRepo.deleteById(reviewId);
        return movieId;
    }
}
