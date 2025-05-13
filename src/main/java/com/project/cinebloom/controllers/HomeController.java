package com.project.cinebloom.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("appName", "CineBloom");
        model.addAttribute("tagline", "Discover and track your favorite movies");
        return "index";
    }
}