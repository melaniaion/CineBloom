package com.project.cinebloom.dtos;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDTO {

    private Long id;
    private String username;
    private String email;

    private String bio;
    private MultipartFile profilePicture;

    private String newPassword;
    private String currentPassword;
}
