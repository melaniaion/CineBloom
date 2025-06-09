package com.project.cinebloom.controllers;

import com.project.cinebloom.dtos.MovieFormDTO;
import com.project.cinebloom.domain.Movie;
import com.project.cinebloom.dtos.MovieSummaryDTO;
import com.project.cinebloom.repositories.CategoryRepository;
import com.project.cinebloom.repositories.MovieRepository;
import com.project.cinebloom.services.MovieService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Controller
@RequestMapping("/movies")
public class MovieController {
    MovieService movieService;
    CategoryRepository categoryRepo;
    MovieRepository movieRepo;
    @Value("classpath:/static/images/default-movie.jpg")
    private Resource defaultPosterImage;


    public MovieController(MovieService movieService,
                           CategoryRepository categoryRepo,
                           MovieRepository movieRepo) {
        this.movieService = movieService;
        this.categoryRepo = categoryRepo;
        this.movieRepo = movieRepo;
    }

    @GetMapping("/movies/{id}/poster")
    public ResponseEntity<byte[]> getPoster(@PathVariable Long id) {
        byte[] pic = movieRepo.findById(id)
                .map(Movie::getPoster)
                .filter(bytes -> bytes != null && bytes.length > 0)
                .orElseGet(() -> {
                    try {
                        return Files.readAllBytes(defaultPosterImage.getFile().toPath());
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to load default poster", e);
                    }
                });

        return ResponseEntity
                .ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(pic);
    }

    @GetMapping("")
    public String movieList(Model model) {
        List<MovieSummaryDTO> movies = movieService.findAll();
        model.addAttribute("movies", movies);
        return "moviesList";
    }

    @GetMapping("/movies/new")
    public String showMovieForm(Model model) {
        model.addAttribute("movie", new MovieFormDTO());
        model.addAttribute("categories", categoryRepo.findAll());
        return "movieForm";
    }

    @PostMapping("/movies/new")
    public String submitMovieForm(@Valid @ModelAttribute("movie") MovieFormDTO dto,
                                  BindingResult bindingResult,
                                  Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryRepo.findAll());
            return "movieForm";
        }

        movieService.createMovie(dto);

        return "redirect:/moviesList";
    }
}
