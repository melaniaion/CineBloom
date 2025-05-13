package com.project.cinebloom.dtos;

import com.project.cinebloom.domain.Category;
import com.project.cinebloom.domain.MovieStats;
import com.project.cinebloom.domain.Review;
import com.project.cinebloom.domain.UserMovie;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MovieDTO {
    private Long id;
    private String title;
    private String poster;
    private LocalDate releaseDate;
    private String description;
    private List<UserMovie> userMovies;
    private List<Review> reviews;
    private Category category;
    private MovieStats stats;
}
