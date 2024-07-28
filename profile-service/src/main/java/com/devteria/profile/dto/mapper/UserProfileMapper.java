package com.devteria.profile.dto.mapper;

import org.mapstruct.Mapper;

import com.devteria.profile.dto.request.ProfileCreationRequest;
import com.devteria.profile.dto.response.UserProfileResponse;
import com.devteria.profile.entity.UserProfile;

@Mapper(componentModel = "spring")
public abstract class UserProfileMapper {
    public abstract UserProfile toModel(ProfileCreationRequest request);

    public abstract UserProfileResponse toDTO(UserProfile entity);
}
