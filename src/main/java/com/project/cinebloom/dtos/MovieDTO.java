package com.project.cinebloom.dtos;


import com.project.cinebloom.domain.WatchStatus;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieDTO {
    private Long id;
    private String title;
    private byte[] poster;
    private LocalDate releaseDate;
    private String description;
    private int duration;
    private String language;
    private String categoryName;

    private int totalFavorites;
    private int totalReviews;
    private WatchStatus userWatchStatus;
    private List<ReviewDTO> reviews;
}
