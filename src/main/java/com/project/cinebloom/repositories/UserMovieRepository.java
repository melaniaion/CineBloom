package com.project.cinebloom.repositories;

import com.project.cinebloom.domain.Movie;
import com.project.cinebloom.domain.User;
import com.project.cinebloom.domain.UserMovie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserMovieRepository extends JpaRepository<UserMovie, Long> {
    Optional<UserMovie> findByUserAndMovie(User user, Movie movie);
}
