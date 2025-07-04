package com.project.cinebloom.repositories;

import com.project.cinebloom.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}