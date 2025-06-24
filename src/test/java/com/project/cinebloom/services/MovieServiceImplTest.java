package com.project.cinebloom.services;

import com.project.cinebloom.domain.*;
import com.project.cinebloom.dtos.MovieDTO;
import com.project.cinebloom.dtos.MovieFormDTO;
import com.project.cinebloom.exceptions.CategoryNotFoundException;
import com.project.cinebloom.exceptions.MovieNotFoundException;
import com.project.cinebloom.mappers.MovieMapper;
import com.project.cinebloom.repositories.CategoryRepository;
import com.project.cinebloom.repositories.MovieRepository;
import com.project.cinebloom.repositories.UserMovieRepository;
import com.project.cinebloom.services.MovieServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieServiceImplTest {

    @Mock private MovieRepository movieRepo;
    @Mock private CategoryRepository categoryRepo;
    @Mock private MovieMapper movieMapper;
    @Mock private UserMovieRepository userMovieRepo;

    @InjectMocks
    private MovieServiceImpl movieService;


    //Create Movie
    @Test
    void createMovie_shouldSaveMovieWhenValidDtoGiven() throws IOException {
        // Arrange
        MovieFormDTO dto = MovieFormDTO.builder()
                .title("Inception")
                .language("English")
                .duration(120)
                .releaseDate(LocalDate.of(2010, 7, 16))
                .description("A mind-bending thriller")
                .categoryId(3L)
                .poster(new MockMultipartFile("poster", "poster.jpg", "image/jpeg", new byte[]{1, 2, 3}))
                .build();

        Movie mappedMovie = new Movie();
        when(movieMapper.toMovie(dto)).thenReturn(mappedMovie);

        Category category = new Category();
        when(categoryRepo.findById(3L)).thenReturn(Optional.of(category));

        // Act
        movieService.createMovie(dto);

        // Assert
        verify(movieMapper).toMovie(dto);
        verify(categoryRepo).findById(3L);
        verify(movieRepo).save(mappedMovie);

        assertSame(category, mappedMovie.getCategory());
        assertNotNull(mappedMovie.getStats());
        assertEquals(0, mappedMovie.getStats().getTotalFavorites());
        assertEquals(0, mappedMovie.getStats().getTotalReviews());
        assertArrayEquals(new byte[]{1, 2, 3}, mappedMovie.getPoster());
    }

    @Test
    void createMovie_shouldThrowExceptionWhenCategoryNotFound() {
        // Arrange
        MovieFormDTO dto = MovieFormDTO.builder()
                .title("Interstellar")
                .language("English")
                .duration(150)
                .releaseDate(LocalDate.of(2014, 11, 7))
                .description("Space exploration")
                .categoryId(99L)
                .poster(null)
                .build();

        Movie mappedMovie = new Movie();
        when(movieMapper.toMovie(dto)).thenReturn(mappedMovie);
        when(categoryRepo.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert
        CategoryNotFoundException exception = assertThrows(
                CategoryNotFoundException.class,
                () -> movieService.createMovie(dto)
        );

        assertEquals("Category not found with id: 99", exception.getMessage());
        verify(movieRepo, never()).save(any());
    }

    @Test
    void createMovie_shouldThrowRuntimeExceptionWhenPosterFails() throws IOException {
        // Arrange
        MultipartFile faultyPoster = mock(MultipartFile.class);
        when(faultyPoster.isEmpty()).thenReturn(false);
        when(faultyPoster.getBytes()).thenThrow(new IOException("Corrupted file"));

        MovieFormDTO dto = MovieFormDTO.builder()
                .title("Tenet")
                .language("English")
                .duration(140)
                .releaseDate(LocalDate.of(2020, 8, 26))
                .description("Time inversion")
                .categoryId(1L)
                .poster(faultyPoster)
                .build();

        Movie mappedMovie = new Movie();
        when(movieMapper.toMovie(dto)).thenReturn(mappedMovie);

        // Act + Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> movieService.createMovie(dto)
        );

        assertTrue(exception.getMessage().contains("Failed to upload poster"));
        assertTrue(exception.getCause() instanceof IOException);
        verify(movieRepo, never()).save(any());
    }

    //Update movie
    @Test
    void updateMovie_shouldUpdateMovieWhenValidDtoGiven() throws IOException {
        // Arrange
        MovieFormDTO dto = MovieFormDTO.builder()
                .id(1L)
                .title("Updated Title")
                .language("Updated Language")
                .duration(130)
                .releaseDate(LocalDate.of(2023, 1, 1))
                .description("Updated description")
                .categoryId(2L)
                .poster(null) // no poster update
                .build();

        Movie movie = new Movie();
        movie.setId(1L); // simulate existing movie

        Category category = new Category();
        category.setId(2L);

        when(movieRepo.findById(1L)).thenReturn(Optional.of(movie));
        when(categoryRepo.findById(2L)).thenReturn(Optional.of(category));

        // Act
        movieService.updateMovie(dto);

        // Assert
        assertEquals("Updated Title", movie.getTitle());
        assertEquals("Updated Language", movie.getLanguage());
        assertEquals(130, movie.getDuration());
        assertEquals(LocalDate.of(2023, 1, 1), movie.getReleaseDate());
        assertEquals("Updated description", movie.getDescription());
        assertSame(category, movie.getCategory());

        verify(movieRepo).save(movie);
    }

    @Test
    void updateMovie_shouldThrowExceptionWhenMovieNotFound() {
        // Arrange
        MovieFormDTO dto = MovieFormDTO.builder()
                .id(999L)
                .title("Doesn't matter")
                .build();

        when(movieRepo.findById(999L)).thenReturn(Optional.empty());

        // Act + Assert
        MovieNotFoundException ex = assertThrows(
                MovieNotFoundException.class,
                () -> movieService.updateMovie(dto)
        );

        assertEquals("Movie not found with id: 999", ex.getMessage());
        verify(movieRepo, never()).save(any());
    }

    //Delete Movie
    @Test
    void deleteById_shouldCallRepository() {
        // Arrange
        Long id = 5L;

        // Act
        movieService.deleteById(id);

        // Assert
        verify(movieRepo, times(1)).deleteById(id);
    }

    //Find Movie By ID
    @Test
    void findById_shouldReturnMovie() {
        // Arrange
        Long movieId = 1L;

        User user = new User();
        user.setId(10L);
        user.setUsername("melania");

        Movie movie = new Movie();
        movie.setId(movieId);
        movie.setTitle("The Prestige");

        Category category = new Category();
        category.setName("Drama");
        movie.setCategory(category);

        MovieStats stats = MovieStats.builder()
                .totalFavorites(5)
                .totalReviews(1)
                .build();
        movie.setStats(stats);
        movie.setReviews(Collections.emptyList());

        UserMovie userMovie = new UserMovie();
        userMovie.setUser(user);
        userMovie.setMovie(movie);
        userMovie.setStatus(WatchStatus.WATCHED);

        when(movieRepo.findById(movieId)).thenReturn(Optional.of(movie));
        when(userMovieRepo.findByUserAndMovie(user, movie)).thenReturn(Optional.of(userMovie));

        // Act
        MovieDTO result = movieService.findById(movieId, user);

        // Assert
        assertEquals(movieId, result.getId());
        assertEquals("The Prestige", result.getTitle());
        assertEquals(WatchStatus.WATCHED, result.getUserWatchStatus());
        assertEquals("Drama", result.getCategoryName());
        assertEquals(5, result.getTotalFavorites());
        assertEquals(1, result.getTotalReviews());
    }

    @Test
    void findById_shouldThrowExceptionWhenMovieNotFound() {
        // Arrange
        Long movieId = 404L;
        when(movieRepo.findById(movieId)).thenReturn(Optional.empty());

        // Act + Assert
        MovieNotFoundException ex = assertThrows(
                MovieNotFoundException.class,
                () -> movieService.findById(movieId, null)
        );

        assertEquals("Movie not found with id: 404", ex.getMessage());
        verify(userMovieRepo, never()).findByUserAndMovie(any(), any());
    }
}

