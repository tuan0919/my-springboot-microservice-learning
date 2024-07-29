package com.devteria.profile.controller;

import org.springframework.web.bind.annotation.*;

import com.devteria.profile.dto.request.ProfileCreationRequest;
import com.devteria.profile.dto.response.UserProfileResponse;
import com.devteria.profile.service.UserProfileService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserProfileController {
    UserProfileService userProfileService;
    @GetMapping("/{profileId}")
    UserProfileResponse getProfile(@PathVariable String profileId) {
        return userProfileService.getProfile(profileId);
    }

    @GetMapping("/")
    List<UserProfileResponse> getAllProfiles() {
        return userProfileService.getAllProfiles();
    }
}
