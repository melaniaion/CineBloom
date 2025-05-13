package com.project.cinebloom.mappers;

import com.project.cinebloom.domain.Movie;
import com.project.cinebloom.dtos.MovieDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MovieMapper {
    MovieDTO toDto (Movie movie);
    Movie toMovie (MovieDTO movieDTO);
}
