package com.medsync.authservice.mapper.user;

import com.medsync.authservice.domain.entity.User;
import com.medsync.authservice.dto.user.response.UserResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(User user);
}