package com.devteria.notification.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.devteria.notification.dto.response.UserProfileResponse;
import com.devteria.notification.service.UserProfileService;

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

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/")
    List<UserProfileResponse> getAllProfiles() {
        return userProfileService.getAllProfiles();
    }
}
