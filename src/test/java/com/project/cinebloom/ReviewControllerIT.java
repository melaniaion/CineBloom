package com.project.cinebloom;

import com.project.cinebloom.domain.*;
import com.project.cinebloom.dtos.ReviewFormDTO;
import com.project.cinebloom.repositories.CategoryRepository;
import com.project.cinebloom.repositories.MovieRepository;
import com.project.cinebloom.repositories.ReviewRepository;
import com.project.cinebloom.repositories.security.AuthorityRepository;
import com.project.cinebloom.repositories.security.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ReviewControllerIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepo;
    @Autowired private MovieRepository movieRepo;
    @Autowired private ReviewRepository reviewRepo;
    @Autowired private AuthorityRepository authorityRepo;
    @Autowired private CategoryRepository categoryRepo;

    private Long movieId;

    @BeforeEach
    void setup() {
        Authority userRole = Authority.builder()
                .role("ROLE_USER")
                .build();
        authorityRepo.save(userRole);

        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .authorities(Set.of(userRole))
                .build();
        userRepo.save(user);

        Set<User> users = new HashSet<>();
        users.add(user);
        userRole.setUsers(users);

        Set<Authority> roles = new HashSet<>();
        roles.add(userRole);
        user.setAuthorities(roles);

        authorityRepo.save(userRole);

        Category category = Category.builder()
                .name("Drama")
                .build();
        categoryRepo.save(category);

        Movie movie = Movie.builder()
                .title("Test Movie")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .language("English")
                .duration(120)
                .description("Simple description")
                .category(category)
                .build();

        MovieStats stats = MovieStats.builder()
                .movie(movie)
                .totalFavorites(0)
                .totalReviews(0)
                .build();

        movie.setStats(stats);
        movie = movieRepo.save(movie);

        movieId = movie.getId();
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void addReview_shouldRedirectOnSuccess() throws Exception {
        try {
            mockMvc.perform(post("/reviews/movie/{movieId}", movieId)
                            .param("value", "4")
                            .param("comment", "This was a good movie!")
                    )
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/movies/" + movieId));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void addReview_shouldRedirectOnDuplicate() throws Exception {
        // Setup: the first review
        Review review = Review.builder()
                .user(userRepo.findByUsername("testuser").orElseThrow())
                .movie(movieRepo.findById(movieId).orElseThrow())
                .value(5)
                .comment("Original review")
                .createdAt(LocalDateTime.now())
                .build();

        reviewRepo.save(review);

        // Attempt to add a second review (should be blocked)
        mockMvc.perform(post("/reviews/movie/{movieId}", movieId)
                        .param("value", "4")
                        .param("comment", "Trying to review again"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/movies/" + movieId + "?error=duplicate"));
    }

    @Test
    void addReview_shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/reviews/movie/{movieId}", movieId)
                        .param("value", "5")
                        .param("comment", "I am anonymous"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void updateReview_shouldSucceedForValidUser() throws Exception {
        User user = userRepo.findByUsername("testuser").orElseThrow();
        Movie movie = movieRepo.findById(movieId).orElseThrow();

        // Save a review owned by that user
        Review review = Review.builder()
                .user(user)
                .movie(movie)
                .value(3)
                .comment("Original comment")
                .createdAt(LocalDateTime.now())
                .build();
        review = reviewRepo.save(review);

        // Act & Assert
        mockMvc.perform(post("/reviews/edit")
                        .param("id", review.getId().toString())
                        .param("value", "5")
                        .param("comment", "Updated comment")
                        .param("movieId", movie.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/movies/" + movieId));
    }

    @Test
    @WithMockUser(username = "otheruser", roles = "USER")
    void updateReview_shouldFailForWrongUser() throws Exception {
        // save a different user as review owner
        User owner = userRepo.findByUsername("testuser").orElseThrow();
        Movie movie = movieRepo.findById(movieId).orElseThrow();

        Review review = Review.builder()
                .user(owner)
                .movie(movie)
                .value(3)
                .comment("Original comment")
                .createdAt(LocalDateTime.now())
                .build();
        review = reviewRepo.save(review);

        // another user tries to update it
        mockMvc.perform(post("/reviews/edit")
                        .param("id", review.getId().toString())
                        .param("value", "4")
                        .param("comment", "Hacked!")
                        .param("movieId", movie.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/movies/" + movieId));
    }

    @Test
    @WithMockUser(username = "otheruser", roles = "USER")
    void deleteReview_shouldRedirectToMoviesForWrongUser() throws Exception {
        User owner = userRepo.findByUsername("testuser").orElseThrow();
        Movie movie = movieRepo.findById(movieId).orElseThrow();

        Review review = Review.builder()
                .user(owner)
                .movie(movie)
                .value(4)
                .comment("Can't touch this")
                .createdAt(LocalDateTime.now())
                .build();
        review = reviewRepo.save(review);

        mockMvc.perform(post("/reviews/delete/{reviewId}", review.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/movies"));
    }
}
