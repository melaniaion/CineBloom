package com.project.cinebloom.controllers;

import com.project.cinebloom.domain.User;
import com.project.cinebloom.dtos.UserProfileDTO;
import com.project.cinebloom.repositories.security.UserRepository;
import com.project.cinebloom.services.security.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.Files;
import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    @Value("classpath:/static/images/default-user.jpg")
    private Resource defaultUserImage;

    @GetMapping("/users/{id}/picture")
    public ResponseEntity<byte[]> getPicture(@PathVariable Long id) {
        byte[] pic = userService.getPictureOrDefault(id, defaultUserImage);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(pic);
    }

    @GetMapping("/profile")
    public String getProfile(Principal principal, Model model) {
        try {
            Long id = userService.getUserId(principal);
            UserProfileDTO dto = userService.getProfile(id);
            model.addAttribute("profile", dto);
        } catch (Exception e) {
            model.addAttribute("ProfileError", e.getMessage());
        }

        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(Principal principal,
                                @ModelAttribute("profile") UserProfileDTO dto,
                                Model model) {

        Long id = userService.getUserId(principal);
        try {
            userService.updateProfile(id, dto);
            model.addAttribute("ProfileUpdateSuccess", "Profile updated successfully");

        } catch (Exception e) {
            model.addAttribute("ProfileUpdateError", e.getMessage());
        }

        model.addAttribute("profile", userService.getProfile(id));
        return "profile";
    }

    @PostMapping("/profile/delete")
    public String deleteAccount(Principal principal,
                                @RequestParam String currentPassword,
                                Model model) {
        try {
            Long id = userService.getUserId(principal);
            userService.deleteAccount(id, currentPassword);
            return "redirect:/perform_logout";
        } catch (Exception e) {
            model.addAttribute("ProfileDeleteError",e.getMessage());
        }
        return "profile";
    }
}
