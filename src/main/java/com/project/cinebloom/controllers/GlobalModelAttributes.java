package com.project.cinebloom.controllers;

import com.project.cinebloom.domain.User;
import com.project.cinebloom.repositories.security.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributes {

    private final UserRepository userRepository;

    @ModelAttribute("userId")
    public Long populateUserId(Principal principal) {
        if (principal == null) {
            return null;
        }
        return userRepository.findByUsername(principal.getName())
                .map(User::getId)
                .orElse(null);
    }
}
