package com.devteria.notification.service;

import org.springframework.stereotype.Service;

import com.devteria.notification.dto.mapper.UserProfileMapper;
import com.devteria.notification.dto.request.ProfileCreationRequest;
import com.devteria.notification.dto.response.UserProfileResponse;
import com.devteria.notification.entity.UserProfile;
import com.devteria.notification.repository.UserProfileRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserProfileService {
    UserProfileRepository profileRepository;
    UserProfileMapper userProfileMapper;

    public UserProfileResponse createProfile(ProfileCreationRequest requestDTO) {
        UserProfile userProfile = userProfileMapper.toModel(requestDTO);
        userProfile = profileRepository.save(userProfile);
        return userProfileMapper.toDTO(userProfile);
    }

    public UserProfileResponse getProfile(String id) {
        var instance = profileRepository.findById(id).orElseThrow(() -> new RuntimeException("Profile is not found"));
        return userProfileMapper.toDTO(instance);
    }

    public List<UserProfileResponse> getAllProfiles() {
        var profiles = profileRepository.findAll();

        return profiles.stream().map(userProfileMapper::toDTO).toList();
    }
}