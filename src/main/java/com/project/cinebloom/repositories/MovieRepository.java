package com.project.cinebloom.repositories;

import com.project.cinebloom.domain.Movie;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Optional;

public interface MovieRepository extends PagingAndSortingRepository<Movie, Long> {
    List<Movie> findAll();
    Optional<Movie> findById (Long id);
    void deleteById(Long id);
    Movie save(Movie product);
}
