package com.project.cinebloom.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewFormDTO {
    private Long id;
    private Long movieId;
    @Min(1)
    @Max(5)
    private int value;

    @Size(max = 300)
    private String comment;
}

