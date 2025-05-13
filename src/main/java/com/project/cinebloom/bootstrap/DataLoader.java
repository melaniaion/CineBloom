package com.project.cinebloom.bootstrap;

import com.project.cinebloom.domain.Authority;
import com.project.cinebloom.domain.User;
import com.project.cinebloom.repositories.security.AuthorityRepository;
import com.project.cinebloom.repositories.security.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class DataLoader implements CommandLineRunner {

    private AuthorityRepository authorityRepository;
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;


    private void loadUserData() {
        if (userRepository.count() == 0){
            Authority adminRole = authorityRepository.save(Authority.builder().role("ROLE_ADMIN").build());
            Authority guestRole = authorityRepository.save(Authority.builder().role("ROLE_USER").build());

            User admin = User.builder()
                    .username("admin")
                    .email("admin@test.com")
                    .password(passwordEncoder.encode("admin01!"))
                    .authority(adminRole)
                    .build();

            User guest = User.builder()
                    .username("user")
                    .email("user@test.com")
                    .password(passwordEncoder.encode("user01!"))
                    .authority(guestRole)
                    .build();

            userRepository.save(admin);
            userRepository.save(guest);
        }
    }


    @Override
    public void run(String... args) throws Exception {
        loadUserData();
    }
}
