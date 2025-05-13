package com.project.cinebloom.services;

import com.project.cinebloom.dtos.MovieDTO;

import java.util.List;

public interface MovieService {
    List<MovieDTO> findAll();
    MovieDTO findById(Long l);
    MovieDTO save(MovieDTO product);
    void deleteById(Long id);
}
