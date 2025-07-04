package com.project.cinebloom.repositories;

import com.project.cinebloom.domain.Review;
import com.project.cinebloom.domain.Movie;
import com.project.cinebloom.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByMovie(Movie movie);
    boolean existsByUserAndMovie(User user, Movie movie);
}

