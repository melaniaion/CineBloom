package com.project.cinebloom.services;

import com.project.cinebloom.domain.Movie;
import com.project.cinebloom.dtos.MovieDTO;
import com.project.cinebloom.mappers.MovieMapper;
import com.project.cinebloom.repositories.MovieRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepo;
    private final MovieMapper movieMapper;

    public MovieServiceImpl(MovieRepository movieRepo,
                            MovieMapper movieMapper) {
        this.movieRepo   = movieRepo;
        this.movieMapper = movieMapper;
    }

    @Override
    public List<MovieDTO> findAll() {
        return StreamSupport.stream(movieRepo.findAll().spliterator(), false)
                .map(movieMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public MovieDTO findById(Long id) {
        Movie Movie = movieRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No Movie with id " + id));
        return movieMapper.toDto(Movie);
    }

    @Override
    public MovieDTO save(MovieDTO dto) {
        // convert DTO → entity, save, then entity → DTO
        Movie MovieToSave = movieMapper.toMovie(dto);
        Movie saved     = movieRepo.save(MovieToSave);
        return movieMapper.toDto(saved);
    }

    @Override
    public void deleteById(Long id) {
        movieRepo.deleteById(id);
    }
}
