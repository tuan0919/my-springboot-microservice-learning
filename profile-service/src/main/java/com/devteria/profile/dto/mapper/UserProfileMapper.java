package com.devteria.profile.dto.mapper;

import com.devteria.profile.dto.request.ProfileCreationRequest;
import com.devteria.profile.dto.response.UserProfileResponse;
import com.devteria.profile.entity.UserProfile;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public abstract class UserProfileMapper {
    public abstract UserProfile toModel(ProfileCreationRequest request);
    public abstract UserProfileResponse toDTO(UserProfile entity);
}
