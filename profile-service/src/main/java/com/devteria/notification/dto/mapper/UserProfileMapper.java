package com.devteria.notification.dto.mapper;

import org.mapstruct.Mapper;

import com.devteria.notification.dto.request.ProfileCreationRequest;
import com.devteria.notification.dto.response.UserProfileResponse;
import com.devteria.notification.entity.UserProfile;

@Mapper(componentModel = "spring")
public abstract class UserProfileMapper {
    public abstract UserProfile toModel(ProfileCreationRequest request);

    public abstract UserProfileResponse toDTO(UserProfile entity);
}
