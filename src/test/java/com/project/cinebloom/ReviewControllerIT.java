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
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
}
