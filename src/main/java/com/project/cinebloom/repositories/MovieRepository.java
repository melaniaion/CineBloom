package com.project.cinebloom.repositories;

import com.project.cinebloom.domain.Movie;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface MovieRepository extends PagingAndSortingRepository<Movie, Long> {
    Page<Movie> findAll(Pageable pageable);
    Optional<Movie> findById (Long id);
    void deleteById(Long id);
    Movie save(Movie product);
    Page<Movie> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Movie> findByTitleContainingIgnoreCaseAndCategory_Id(String title, Long categoryId, Pageable pageable);
}
