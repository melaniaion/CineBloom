package com.project.cinebloom.dtos;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieSummaryDTO {
    private Long id;
    private String title;
    private MultipartFile poster;
    private LocalDate releaseDate;
    private String categoryName;
}