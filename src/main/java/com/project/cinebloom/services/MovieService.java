package com.project.cinebloom.services;

import com.project.cinebloom.dtos.MovieDTO;
import com.project.cinebloom.dtos.MovieFormDTO;
import com.project.cinebloom.dtos.MovieSummaryDTO;

import java.util.List;

public interface MovieService {
    List<MovieSummaryDTO> findAll();
    void createMovie(MovieFormDTO dto);
    void deleteById(Long id);
}
