package com.project.cinebloom.services;

import com.project.cinebloom.domain.User;
import com.project.cinebloom.domain.WatchStatus;
import com.project.cinebloom.dtos.MovieSummaryDTO;
import org.springframework.data.domain.Page;

public interface UserMovieService {
    void addToWatchList(String username, Long movieId, WatchStatus status);
    Page<MovieSummaryDTO> getUserMoviesByStatus(User user, WatchStatus status, int page, String sort);
    void removeMovieFromList(User user, Long movieId, WatchStatus status);
    void moveMovieToList(User user, Long movieId, WatchStatus newStatus);
}
