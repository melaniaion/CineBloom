package com.project.cinebloom.repositories;

import com.project.cinebloom.domain.Movie;
import com.project.cinebloom.domain.MovieStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MovieStatsRepository extends JpaRepository<MovieStats, Long> {
    Optional<MovieStats> findByMovie(Movie movie);

}

