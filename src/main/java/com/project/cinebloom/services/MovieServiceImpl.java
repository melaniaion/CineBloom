package com.project.cinebloom.services;

import com.project.cinebloom.domain.*;
import com.project.cinebloom.dtos.MovieDTO;
import com.project.cinebloom.dtos.MovieFormDTO;
import com.project.cinebloom.dtos.MovieSummaryDTO;
import com.project.cinebloom.dtos.ReviewDTO;
import com.project.cinebloom.exceptions.CategoryNotFoundException;
import com.project.cinebloom.exceptions.MovieNotFoundException;
import com.project.cinebloom.logging.LogAdminAction;
import com.project.cinebloom.mappers.MovieMapper;
import com.project.cinebloom.repositories.CategoryRepository;
import com.project.cinebloom.repositories.MovieRepository;
import com.project.cinebloom.repositories.UserMovieRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepo;
    private final MovieMapper movieMapper;
    private final CategoryRepository categoryRepo;
    private final UserMovieRepository userMovieRepo;

    public MovieServiceImpl(MovieRepository movieRepo,
                            MovieMapper movieMapper,
                            CategoryRepository categoryRepo,
                            UserMovieRepository userMovieRepo) {
        this.movieRepo   = movieRepo;
        this.movieMapper = movieMapper;
        this.categoryRepo = categoryRepo;
        this.userMovieRepo = userMovieRepo;
    }

    @Override
    public Page<MovieSummaryDTO> findPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("title").ascending());
        Page<Movie> moviePage = movieRepo.findAll(pageable);
        return moviePage.map(movieMapper::toSummaryDto);
    }

    @Override
    public Page<MovieSummaryDTO> findFiltered(String title, Long categoryId, String sortField, String sortDir, int page, int size) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        Page<Movie> moviePage;

        if (categoryId != null) {
            moviePage = movieRepo.findByTitleContainingIgnoreCaseAndCategory_Id(title, categoryId, pageable);
        } else {
            moviePage = movieRepo.findByTitleContainingIgnoreCase(title, pageable);
        }

        return moviePage.map(movieMapper::toSummaryDto);
    }

    @Override
    public MovieDTO findById(Long movieId, User user) {
        Movie movie = movieRepo.findById(movieId)
                .orElseThrow(() -> new MovieNotFoundException(movieId));

        WatchStatus status = null;
        if (user != null) {
            Optional<UserMovie> userMovieOpt = userMovieRepo.findByUserAndMovie(user, movie);
            if (userMovieOpt.isPresent()) {
                status = userMovieOpt.get().getStatus();
            }
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm", Locale.ENGLISH);

        List<ReviewDTO> reviewDTOs = movie.getReviews().stream()
                .map(review -> ReviewDTO.builder()
                        .id(review.getId())
                        .userId(review.getUser().getId())
                        .username(review.getUser().getUsername())
                        .value(review.getValue())
                        .comment(review.getComment())
                        .createdAt(review.getCreatedAt())
                        .build())
                .toList();

        return MovieDTO.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .poster(movie.getPoster())
                .releaseDate(movie.getReleaseDate())
                .description(movie.getDescription())
                .duration(movie.getDuration())
                .language(movie.getLanguage())
                .categoryName(movie.getCategory().getName())
                .totalFavorites(movie.getStats().getTotalFavorites())
                .totalReviews(movie.getStats().getTotalReviews())
                .userWatchStatus(status)
                .reviews(reviewDTOs)
                .build();
    }

    @LogAdminAction(action = "CREATE_MOVIE")
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
                .orElseThrow(() -> new CategoryNotFoundException(dto.getCategoryId()));
        movie.setCategory(category);

        MovieStats stats = MovieStats.builder()
                .movie(movie)
                .totalFavorites(0)
                .totalReviews(0)
                .build();
        movie.setStats(stats);

        movieRepo.save(movie);
    }

    @LogAdminAction(action = "DELETE_MOVIE")
    @Override
    public void deleteById(Long id) {
        movieRepo.deleteById(id);
    }

    @Override
    public MovieFormDTO getMovieFormById(Long id) {
        Movie movie = movieRepo.findById(id)
                .orElseThrow(() -> new MovieNotFoundException(id));
        return movieMapper.toFormDto(movie);
    }

    @LogAdminAction(action = "UPDATE_MOVIE")
    @Override
    public void updateMovie(MovieFormDTO dto) {
        Movie movie = movieRepo.findById(dto.getId())
                .orElseThrow(() -> new MovieNotFoundException(dto.getId()));

        movie.setTitle(dto.getTitle());
        movie.setLanguage(dto.getLanguage());
        movie.setDuration(dto.getDuration());
        movie.setReleaseDate(dto.getReleaseDate());
        movie.setDescription(dto.getDescription());

        Category category = categoryRepo.findById(dto.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException(dto.getCategoryId()));
        movie.setCategory(category);

        if (dto.getPoster() != null && !dto.getPoster().isEmpty()) {
            try {
                movie.setPoster(dto.getPoster().getBytes());
            } catch (IOException e) {
                throw new RuntimeException("Failed to read poster file", e);
            }
        }
        movieRepo.save(movie);
    }
}
