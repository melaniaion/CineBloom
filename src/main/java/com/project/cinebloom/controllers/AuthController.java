package com.project.cinebloom.controllers;

import com.project.cinebloom.services.security.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import com.project.cinebloom.dtos.UserRegistrationDTO;
import jakarta.validation.Valid;

import java.io.IOException;

@Controller
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("appName", "CineBloom");
        return "login";
    }

    @GetMapping("/access_denied")
    public String accessDenied() {
        return "access_denied";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("appName", "CineBloom");
        model.addAttribute("userDto", new UserRegistrationDTO());
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @ModelAttribute("userDto") @Valid UserRegistrationDTO dto,
            BindingResult br,
            Model m
    ) throws IOException {
        m.addAttribute("appName","CineBloom");
        if(!dto.getPassword().equals(dto.getConfirmPassword()))
            br.rejectValue("confirmPassword","password.mismatch","Passwords must match");
        userService.register(dto,br);
        if(br.hasErrors()) return "register";
        return "redirect:/login?registered";
    }
}
