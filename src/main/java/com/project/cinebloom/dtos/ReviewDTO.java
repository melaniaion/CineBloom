package com.project.cinebloom.dtos;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDTO {
    private Long id;
    private Long userId;
    private String username;
    private int value;
    private String comment;
    private LocalDateTime createdAt;
}


