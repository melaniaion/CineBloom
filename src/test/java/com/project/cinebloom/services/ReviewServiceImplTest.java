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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private UserRepository userRepo;
    @Mock private MovieRepository movieRepo;
    @Mock private ReviewRepository reviewRepo;
    @Mock private MovieStatsRepository statsRepo;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    @Test
    void addReview_shouldSaveReviewWhenValidInput() {
        // Arrange
        Long movieId = 1L;
        String username = "melania";

        ReviewFormDTO form = ReviewFormDTO.builder()
                .value(5)
                .comment("Amazing movie!")
                .build();

        User user = new User();
        user.setUsername(username);

        MovieStats stats = MovieStats.builder()
                .totalReviews(2)
                .build();

        Movie movie = new Movie();
        movie.setStats(stats);

        when(userRepo.findByUsername(username)).thenReturn(Optional.of(user));
        when(movieRepo.findById(movieId)).thenReturn(Optional.of(movie));
        when(reviewRepo.existsByUserAndMovie(user, movie)).thenReturn(false);

        // Act
        reviewService.addReview(movieId, username, form);

        // Assert
        verify(reviewRepo).save(any(Review.class));
        verify(statsRepo).save(stats);
        assertEquals(3, stats.getTotalReviews());
    }

    @Test
    void addReview_shouldThrowExceptionWhenMovieNotFound() {
        // Arrange
        Long movieId = 999L;
        String username = "melania";

        ReviewFormDTO form = ReviewFormDTO.builder()
                .value(4)
                .comment("Doesn't matter")
                .build();

        User user = new User();
        user.setUsername(username);

        when(userRepo.findByUsername(username)).thenReturn(Optional.of(user));
        when(movieRepo.findById(movieId)).thenReturn(Optional.empty());

        // Act + Assert
        MovieNotFoundException ex = assertThrows(
                MovieNotFoundException.class,
                () -> reviewService.addReview(movieId, username, form)
        );

        assertEquals("Movie not found with id: 999", ex.getMessage());
        verify(reviewRepo, never()).save(any());
        verify(statsRepo, never()).save(any());
    }

    @Test
    void addReview_shouldThrowExceptionWhenReviewAlreadyExists() {
        // Arrange
        Long movieId = 1L;
        String username = "melania";

        ReviewFormDTO form = ReviewFormDTO.builder()
                .value(4)
                .comment("Already reviewed")
                .build();

        User user = new User();
        user.setUsername(username);

        Movie movie = new Movie();

        when(userRepo.findByUsername(username)).thenReturn(Optional.of(user));
        when(movieRepo.findById(movieId)).thenReturn(Optional.of(movie));
        when(reviewRepo.existsByUserAndMovie(user, movie)).thenReturn(true);

        // Act + Assert
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> reviewService.addReview(movieId, username, form)
        );

        assertEquals("User has already reviewed this movie", ex.getMessage());
        verify(reviewRepo, never()).save(any());
        verify(statsRepo, never()).save(any());
    }

    @Test
    void updateReview_shouldUpdateReviewWhenUserIsOwner() {
        // Arrange
        Long reviewId = 1L;
        String username = "melania";

        ReviewFormDTO form = ReviewFormDTO.builder()
                .id(reviewId)
                .value(4)
                .comment("Updated comment")
                .build();

        User user = new User();
        user.setUsername(username);

        Review review = new Review();
        review.setId(reviewId);
        review.setUser(user);

        when(reviewRepo.findById(reviewId)).thenReturn(Optional.of(review));

        // Act
        reviewService.updateReview(form, username);

        // Assert
        assertEquals(4, review.getValue());
        assertEquals("Updated comment", review.getComment());
        verify(reviewRepo).save(review);
    }

    @Test
    void updateReview_shouldThrowExceptionWhenReviewNotFound() {
        // Arrange
        Long reviewId = 99L;
        String username = "melania";

        ReviewFormDTO form = ReviewFormDTO.builder()
                .id(reviewId)
                .value(4)
                .comment("Doesn't matter")
                .build();

        when(reviewRepo.findById(reviewId)).thenReturn(Optional.empty());

        // Act + Assert
        ReviewNotFoundException ex = assertThrows(
                ReviewNotFoundException.class,
                () -> reviewService.updateReview(form, username)
        );

        assertEquals("Review not found with id: 99", ex.getMessage());
        verify(reviewRepo, never()).save(any());
    }

    @Test
    void updateReview_shouldThrowExceptionWhenUserIsNotOwner() {
        // Arrange
        Long reviewId = 1L;
        String username = "intruder";

        ReviewFormDTO form = ReviewFormDTO.builder()
                .id(reviewId)
                .value(5)
                .comment("Trying to hack")
                .build();

        User actualOwner = new User();
        actualOwner.setUsername("melania");

        Review review = new Review();
        review.setId(reviewId);
        review.setUser(actualOwner); // not the same as current user

        when(reviewRepo.findById(reviewId)).thenReturn(Optional.of(review));

        // Act + Assert
        AccessDeniedException ex = assertThrows(
                AccessDeniedException.class,
                () -> reviewService.updateReview(form, username)
        );

        assertEquals("User not authorized", ex.getMessage());
        verify(reviewRepo, never()).save(any());
    }

    @Test
    void deleteReview_shouldDeleteReviewAndDecrementStats() {
        // Arrange
        Long reviewId = 1L;
        String username = "melania";
        Long movieId = 42L;

        User user = new User();
        user.setUsername(username);

        MovieStats stats = MovieStats.builder().totalReviews(5).build();

        Movie movie = new Movie();
        movie.setId(movieId);
        movie.setStats(stats);

        Review review = new Review();
        review.setId(reviewId);
        review.setUser(user);
        review.setMovie(movie);

        when(reviewRepo.findById(reviewId)).thenReturn(Optional.of(review));

        // Act
        Long returnedMovieId = reviewService.deleteReview(reviewId, username);

        // Assert
        assertEquals(42L, returnedMovieId);
        assertEquals(4, stats.getTotalReviews());
        verify(reviewRepo).deleteById(reviewId);
        verify(statsRepo).save(stats);
    }

    @Test
    void deleteReview_shouldThrowExceptionWhenReviewNotFound() {
        // Arrange
        Long reviewId = 99L;
        String username = "melania";

        when(reviewRepo.findById(reviewId)).thenReturn(Optional.empty());

        // Act + Assert
        ReviewNotFoundException ex = assertThrows(
                ReviewNotFoundException.class,
                () -> reviewService.deleteReview(reviewId, username)
        );

        assertEquals("Review not found with id: 99", ex.getMessage());
        verify(reviewRepo, never()).deleteById(any());
    }

    @Test
    void deleteReview_shouldThrowExceptionWhenUserIsNotOwner() {
        // Arrange
        Long reviewId = 1L;
        String username = "intruder";

        User actualOwner = new User();
        actualOwner.setUsername("melania");

        Movie movie = new Movie();
        movie.setId(42L);
        movie.setStats(MovieStats.builder().totalReviews(5).build());

        Review review = new Review();
        review.setId(reviewId);
        review.setUser(actualOwner);
        review.setMovie(movie);

        when(reviewRepo.findById(reviewId)).thenReturn(Optional.of(review));

        // Act + Assert
        AccessDeniedException ex = assertThrows(
                AccessDeniedException.class,
                () -> reviewService.deleteReview(reviewId, username)
        );

        assertEquals("User not authorized", ex.getMessage());
        verify(reviewRepo, never()).deleteById(any());
        verify(statsRepo, never()).save(any());
    }
}
