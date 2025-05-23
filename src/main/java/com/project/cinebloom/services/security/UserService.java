package com.project.cinebloom.services.security;

import com.project.cinebloom.domain.Authority;
import com.project.cinebloom.domain.User;
import com.project.cinebloom.dtos.UserRegistrationDTO;
import com.project.cinebloom.repositories.security.AuthorityRepository;
import com.project.cinebloom.repositories.security.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.io.IOException;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepo;
    private final AuthorityRepository authRepo;
    private final PasswordEncoder passwordEncoder;

    public void register(UserRegistrationDTO dto, BindingResult br) throws IOException {
        if(userRepo.existsByUsername(dto.getUsername()))
            br.rejectValue("username","username.exists","That username is taken");
        if(userRepo.existsByEmail(dto.getEmail()))
            br.rejectValue("email","email.exists","That email is already registered");
        if(br.hasErrors()) return;

        User user = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .bio(dto.getBio())
                .profilePicture(
                        dto.getProfilePicture()!=null && !dto.getProfilePicture().isEmpty()
                                ? dto.getProfilePicture().getBytes()
                                : null
                )
                .build();

        Authority userRole = authRepo.findByRole("ROLE_USER");
        user.setAuthorities(Set.of(userRole));
        userRepo.save(user);
    }
}

