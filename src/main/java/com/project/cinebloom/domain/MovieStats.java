// MovieStats entity
package com.project.cinebloom.domain;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
public class MovieStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "movie_id", unique = true)
    private Movie movie;

    private Double averageRating;
    private int totalReviews;
    private int totalFavorites;
}
