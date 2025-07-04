package com.project.cinebloom.mappers;

import com.project.cinebloom.domain.Movie;
import com.project.cinebloom.dtos.MovieDTO;
import com.project.cinebloom.dtos.MovieSummaryDTO;
import com.project.cinebloom.dtos.MovieFormDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MovieMapper {
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(target = "poster", ignore = true)
    MovieSummaryDTO toSummaryDto(Movie movie);
    List<MovieSummaryDTO> toSummaryDtoList(List<Movie> movies);

    @Mapping(target = "poster", ignore = true)
    @Mapping(source = "categoryId", target = "category.id")
    Movie toMovie(MovieFormDTO dto);

    @Mapping(target = "poster", ignore = true)
    @Mapping(source = "category.id", target = "categoryId")
    MovieFormDTO toFormDto(Movie movie);
}
