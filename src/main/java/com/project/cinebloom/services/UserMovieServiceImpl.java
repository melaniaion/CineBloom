package com.project.cinebloom.services;

import com.project.cinebloom.domain.*;
import com.project.cinebloom.dtos.MovieSummaryDTO;
import com.project.cinebloom.mappers.MovieMapper;
import com.project.cinebloom.repositories.MovieRepository;
import com.project.cinebloom.repositories.MovieStatsRepository;
import com.project.cinebloom.repositories.UserMovieRepository;
import com.project.cinebloom.services.security.UserService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class UserMovieServiceImpl implements UserMovieService {
    private final UserMovieRepository userMovieRepo;
    private final UserService userService;
    private final MovieRepository movieRepo;
    private final MovieStatsRepository statsRepo;
    private final MovieMapper movieMapper;

    @Override
    public void addToWatchList(String username, Long movieId, WatchStatus status) {
        User user = userService.findByUsername(username).orElse(null);

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
    }
    @Override
    public Page<MovieSummaryDTO> getUserMoviesByStatus(User user, WatchStatus status, int page, String sort) {
        Sort sorting = sort.equalsIgnoreCase("asc")
                ? Sort.by("addedAt").ascending()
                : Sort.by("addedAt").descending();

        Pageable pageable = PageRequest.of(page, 6, sorting);

        Page<UserMovie> userMovies = userMovieRepo.findByUserAndStatus(user, status, pageable);

        return userMovies.map(um -> movieMapper.toSummaryDto(um.getMovie()));
    }

    @Override
    public void removeMovieFromList(User user, Long movieId, WatchStatus status) {
        UserMovie userMovie = userMovieRepo.findByUserAndMovie(user, Movie.builder().id(movieId).build())
                .orElseThrow(() -> new IllegalArgumentException("Movie not found in your list"));

        if (userMovie.getStatus() != status) {
            throw new IllegalArgumentException("Movie is not in the expected list");
        }

        userMovieRepo.delete(userMovie);
    }

    @Override
    public void moveMovieToList(User user, Long movieId, WatchStatus newStatus) {
        UserMovie userMovie = userMovieRepo.findByUserAndMovie(user, Movie.builder().id(movieId).build())
                .orElseThrow(() -> new IllegalArgumentException("Movie not found in your list"));

        if (userMovie.getStatus() == newStatus) {
            throw new IllegalArgumentException("Movie is already in the selected list");
        }

        userMovie.setStatus(newStatus);
        userMovie.setAddedAt(LocalDateTime.now());
        userMovieRepo.save(userMovie);
    }

}
