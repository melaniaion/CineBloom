package com.project.cinebloom.mappers;

import com.project.cinebloom.domain.User;
import com.project.cinebloom.dtos.UserProfileDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "profilePicture", ignore = true)
    UserProfileDTO toDto(User user);
    @Mapping(target = "profilePicture", ignore = true)
    User toUser(UserProfileDTO userProfileDTO);

}

