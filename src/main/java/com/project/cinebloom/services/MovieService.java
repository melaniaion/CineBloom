package com.project.cinebloom.services;

import com.project.cinebloom.domain.User;
import com.project.cinebloom.dtos.MovieDTO;
import com.project.cinebloom.dtos.MovieFormDTO;
import com.project.cinebloom.dtos.MovieSummaryDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface MovieService {
    Page<MovieSummaryDTO> findPaginated(int page, int size);
    public MovieDTO findById(Long movieId, User user);
    void createMovie(MovieFormDTO dto);
    void deleteById(Long id);
    Page<MovieSummaryDTO> findFiltered(String title, Long categoryId, String sortField, String sortDir, int page, int size);
    MovieFormDTO getMovieFormById(Long id);
    void updateMovie(MovieFormDTO dto);
    byte[] getPosterOrDefault(Long id);

}
