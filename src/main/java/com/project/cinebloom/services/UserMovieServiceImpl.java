package com.project.cinebloom.services;

import com.project.cinebloom.domain.Movie;
import com.project.cinebloom.domain.User;
import com.project.cinebloom.domain.UserMovie;
import com.project.cinebloom.domain.WatchStatus;
import com.project.cinebloom.dtos.MovieSummaryDTO;
import com.project.cinebloom.mappers.MovieMapper;
import com.project.cinebloom.repositories.UserMovieRepository;
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
    private final MovieMapper movieMapper;
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
