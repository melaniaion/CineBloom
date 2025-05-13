package com.project.cinebloom.controllers;

import com.project.cinebloom.dtos.MovieDTO;
import com.project.cinebloom.services.MovieService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/movies")
public class MovieController {
    MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @RequestMapping("")
    public String MovieList(Model model) {
        List<MovieDTO> movies = movieService.findAll();
        model.addAttribute("movies", movies);
        return "moviesList";
    }
}
