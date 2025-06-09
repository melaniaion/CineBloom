package com.project.cinebloom.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
public class Movie {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;
    @Lob
    private byte[] poster;
    private LocalDate releaseDate;
    private String language;

    @Lob
    private String description;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserMovie> userMovies;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews;

    @ManyToOne()
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToOne(mappedBy = "movie", cascade = CascadeType.ALL)
    private MovieStats stats;
}
