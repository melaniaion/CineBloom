package com.project.cinebloom.repositories;

import com.project.cinebloom.domain.Movie;
import com.project.cinebloom.domain.User;
import com.project.cinebloom.domain.UserMovie;
import com.project.cinebloom.domain.WatchStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserMovieRepository extends JpaRepository<UserMovie, Long> {
    Optional<UserMovie> findByUserAndMovie(User user, Movie movie);
    Page<UserMovie> findByUserAndStatus(User user, WatchStatus status, Pageable pageable);
}
