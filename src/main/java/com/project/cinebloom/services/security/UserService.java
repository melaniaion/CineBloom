package com.project.cinebloom.services.security;

import com.project.cinebloom.domain.Authority;
import com.project.cinebloom.domain.User;
import com.project.cinebloom.dtos.UserProfileDTO;
import com.project.cinebloom.dtos.UserRegistrationDTO;
import com.project.cinebloom.mappers.UserMapper;
import com.project.cinebloom.repositories.security.AuthorityRepository;
import com.project.cinebloom.repositories.security.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.io.IOException;
import java.nio.file.Files;
import java.security.Principal;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepo;
    private final AuthorityRepository authRepo;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    @Value("classpath:/static/images/default-user.jpg")
    private Resource defaultUserImage;

    public Optional<User> findByUsername(String username) {
        return userRepo.findByUsername(username);
    }

    public Long getUserId(Principal principal) {
        if (principal == null)
            throw new UsernameNotFoundException("No authenticated user");

        return userRepo.findByUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"))
                .getId();
    }

    public byte[] getPictureOrDefault(Long id, Resource defaultImage) {
        return userRepo.findById(id)
                .map(User::getProfilePicture)
                .filter(p -> p != null && p.length > 0)
                .orElseGet(() -> {
                    try {
                        return Files.readAllBytes(defaultUserImage.getFile().toPath());
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to load default image", e);
                    }
                });
    }

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

    public UserProfileDTO getProfile(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return userMapper.toDto(user);
    }

    public void updateProfile(Long userId, UserProfileDTO dto) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (dto.getBio() != null && !dto.getBio().isBlank()) {
            user.setBio(dto.getBio());
        }

        if (dto.getProfilePicture() != null && !dto.getProfilePicture().isEmpty()) {
            try {
                user.setProfilePicture(dto.getProfilePicture().getBytes());
            } catch (IOException e) {
                throw new RuntimeException("Failed to process profile picture");
            }
        }

        boolean isChangingPassword =
                dto.getCurrentPassword() != null && !dto.getCurrentPassword().isBlank() &&
                        dto.getNewPassword() != null && !dto.getNewPassword().isBlank();

        if (isChangingPassword) {
            if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
                throw new IllegalArgumentException("Incorrect current password");
            }
            user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        }

        userRepo.save(user);
    }

    public void deleteAccount(Long userId, String password) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Incorrect password");
        }

        userRepo.delete(user);
    }
}

