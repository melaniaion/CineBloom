package com.project.cinebloom.controllers;

import com.project.cinebloom.domain.User;
import com.project.cinebloom.domain.WatchStatus;
import com.project.cinebloom.dtos.MovieDTO;
import com.project.cinebloom.dtos.MovieFormDTO;
import com.project.cinebloom.domain.Movie;
import com.project.cinebloom.dtos.MovieSummaryDTO;
import com.project.cinebloom.dtos.ReviewFormDTO;
import com.project.cinebloom.repositories.CategoryRepository;
import com.project.cinebloom.repositories.MovieRepository;
import com.project.cinebloom.repositories.security.UserRepository;
import com.project.cinebloom.services.MovieService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/movies")
public class MovieController {
    MovieService movieService;
    CategoryRepository categoryRepo;
    MovieRepository movieRepo;
    UserRepository userRepo;
    @Value("classpath:/static/images/default-movie.jpg")
    private Resource defaultPosterImage;


    public MovieController(MovieService movieService,
                           CategoryRepository categoryRepo,
                           MovieRepository movieRepo,
                           UserRepository userRepo) {
        this.movieService = movieService;
        this.categoryRepo = categoryRepo;
        this.movieRepo = movieRepo;
        this.userRepo = userRepo;
    }


    @GetMapping("/{id}/poster")
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


    @GetMapping
    public String movieList(@RequestParam(defaultValue = "") String title,
                            @RequestParam(required = false) Long categoryId,
                            @RequestParam(defaultValue = "title_asc") String sortOption,
                            @RequestParam(defaultValue = "0") int page,
                            Model model) {

        int size = 6;

        String[] parts = sortOption.split("_");
        String sort = parts[0];
        String dir = parts[1];

        Page<MovieSummaryDTO> moviePage = movieService.findFiltered(title, categoryId, sort, dir, page, size);

        model.addAttribute("movies", moviePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", moviePage.getTotalPages());
        model.addAttribute("hasNext", moviePage.hasNext());
        model.addAttribute("hasPrevious", moviePage.hasPrevious());

        model.addAttribute("title", title);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);
        model.addAttribute("sortOption", sortOption);
        model.addAttribute("categories", categoryRepo.findAll());

        return "moviesList";
    }


    @GetMapping("/{id}")
    public String movieDetails(@PathVariable Long id,
                               Principal principal,
                               @RequestParam(required = false) String error,
                               Model model) {

        User user = null;
        if (principal != null) {
            user = userRepo.findByUsername(principal.getName())
                    .orElse(null);
        }

        MovieDTO movie = movieService.findById(id, user);

        model.addAttribute("movie", movie);
        model.addAttribute("statuses", WatchStatus.values());
        model.addAttribute("reviewForm", new ReviewFormDTO());

        // Handle optional error message (from redirect)
        if ("form".equals(error)) {
            model.addAttribute("reviewError", "Please correct the review form.");
        } else if ("duplicate".equals(error)) {
            model.addAttribute("reviewError", "You have already reviewed this movie.");
        }

        return "movieDetails";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/new")
    public String addMovie(Model model) {
        model.addAttribute("movie", new MovieFormDTO());
        model.addAttribute("categories", categoryRepo.findAll());
        return "movieForm";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/new")
    public String submitMovieForm(@Valid @ModelAttribute("movie") MovieFormDTO dto,
                                  BindingResult bindingResult,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryRepo.findAll());
            return "movieForm";
        }

        try {
            movieService.createMovie(dto);
            redirectAttributes.addFlashAttribute("successMovie", "Movie added successfully.");
            return "redirect:/movies";
        } catch (Exception e) {
            model.addAttribute("errorMovie", e.getMessage());
            model.addAttribute("categories", categoryRepo.findAll());
            return "movieForm";
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/delete")
    public String deleteMovie(@RequestParam Long id,
                              RedirectAttributes redirectAttributes) {
        try {
            movieService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMovie", "Movie deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMovie", e.getMessage());
        }
        return "redirect:/movies";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/edit/{id}")
    public String editMovie(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            MovieFormDTO dto = movieService.getMovieFormById(id);
            model.addAttribute("movie", dto);
            model.addAttribute("categories", categoryRepo.findAll());
            return "editMovie";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMovie", e.getMessage());
            return "redirect:/movies";
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/edit")
    public String updateMovie(@Valid @ModelAttribute("movie") MovieFormDTO dto,
                              BindingResult result,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryRepo.findAll());
            return "editMovie";
        }

        try {
            movieService.updateMovie(dto);
            redirectAttributes.addFlashAttribute("successMovie", "Movie edited successfully.");
            return "redirect:/movies";
        } catch (Exception e) {
            model.addAttribute("errorMovie", e.getMessage());
            model.addAttribute("categories", categoryRepo.findAll());
            return "editMovie";
        }
    }
}
