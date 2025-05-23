package com.project.cinebloom.controllers;

import com.project.cinebloom.domain.User;
import com.project.cinebloom.repositories.security.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.Files;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepo;
    @Value("classpath:/static/images/default-user.jpg")
    private Resource defaultUserImage;

    @GetMapping("/users/{id}/picture")
    public ResponseEntity<byte[]> getPicture(@PathVariable Long id) {
        byte[] pic = userRepo.findById(id)
                .map(User::getProfilePicture)
                .filter(bytes -> bytes != null && bytes.length > 0)
                .orElseGet(() -> {
                    try {
                        return Files.readAllBytes(defaultUserImage.getFile().toPath());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

        // detect your real image type if you need PNG vs JPEG
        return ResponseEntity
                .ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(pic);
    }
}
