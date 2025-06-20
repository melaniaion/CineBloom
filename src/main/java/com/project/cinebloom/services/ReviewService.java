package com.project.cinebloom.services;

import com.project.cinebloom.domain.User;
import com.project.cinebloom.dtos.ReviewFormDTO;

public interface ReviewService {
    void addReview(Long movieId, User user, ReviewFormDTO dto);
}
