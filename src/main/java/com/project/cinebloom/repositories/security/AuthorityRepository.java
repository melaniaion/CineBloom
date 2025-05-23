package com.project.cinebloom.repositories.security;

import com.project.cinebloom.domain.Authority;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorityRepository extends JpaRepository<Authority, Long> {
    Authority findByRole(String role);
}
