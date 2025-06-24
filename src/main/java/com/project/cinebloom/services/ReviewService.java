package com.project.cinebloom.services;

import com.project.cinebloom.domain.User;
import com.project.cinebloom.dtos.ReviewFormDTO;

public interface ReviewService {
    void addReview(Long movieId, String username, ReviewFormDTO dto);
    void updateReview(ReviewFormDTO form, String username);
    Long deleteReview(Long reviewId, String username);
}
