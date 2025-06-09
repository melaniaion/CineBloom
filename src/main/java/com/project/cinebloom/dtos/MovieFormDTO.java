package com.project.cinebloom.dtos;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieFormDTO {

    private Long id;

    @NotBlank(message = "Title is required")
    @Size(min = 3, message = "Title must be at least 3 characters")
    private String title;
    private MultipartFile poster;

    @Size(min = 6, message = "Description must be at least 6 characters")
    private String description;

    @NotNull(message = "Release date is required")
    @PastOrPresent(message = "Release date cannot be in the future")
    private LocalDate releaseDate;

    @NotBlank(message = "Language is required")
    private String language;

    @NotNull(message = "Category is required")
    private Long categoryId;
}