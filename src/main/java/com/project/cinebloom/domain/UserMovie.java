package com.project.cinebloom.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_movie")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserMovie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id",  nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WatchStatus status;
    private boolean favorite;

    @Column(nullable = false)
    private LocalDateTime addedAt;

    @PrePersist
    public void prePersist() {
        if (this.addedAt == null) {
            this.addedAt = LocalDateTime.now();
        }
    }
}
