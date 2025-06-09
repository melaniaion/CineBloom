package com.project.cinebloom.services;

import com.project.cinebloom.domain.Category;
import com.project.cinebloom.domain.Movie;
import com.project.cinebloom.domain.MovieStats;
import com.project.cinebloom.dtos.MovieDTO;
import com.project.cinebloom.dtos.MovieFormDTO;
import com.project.cinebloom.dtos.MovieSummaryDTO;
import com.project.cinebloom.mappers.MovieMapper;
import com.project.cinebloom.repositories.CategoryRepository;
import com.project.cinebloom.repositories.MovieRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepo;
    private final MovieMapper movieMapper;
    private final CategoryRepository categoryRepo;

    public MovieServiceImpl(MovieRepository movieRepo,
                            MovieMapper movieMapper,
                            CategoryRepository categoryRepo) {
        this.movieRepo   = movieRepo;
        this.movieMapper = movieMapper;
        this.categoryRepo = categoryRepo;
    }

    @Override
    public List<MovieSummaryDTO> findAll() {
        List<Movie> movies = movieRepo.findAll();
        return movieMapper.toSummaryDtoList(movies);
    }

    @Override
    public void createMovie(MovieFormDTO dto) {
        Movie movie = movieMapper.toMovie(dto);

        if (dto.getPoster() != null && !dto.getPoster().isEmpty()) {
            try {
                movie.setPoster(dto.getPoster().getBytes());
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload poster", e);
            }
        }

        Category category = categoryRepo.findById(dto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid category ID"));
        movie.setCategory(category);

        MovieStats stats = MovieStats.builder()
                .movie(movie)
                .totalFavorites(0)
                .totalReviews(0)
                .build();
        movie.setStats(stats);

        movieRepo.save(movie);
    }

    @Override
    public void deleteById(Long id) {
        movieRepo.deleteById(id);
    }
}
