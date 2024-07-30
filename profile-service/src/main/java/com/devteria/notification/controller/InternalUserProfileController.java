package com.devteria.notification.controller;

import com.devteria.notification.dto.request.ProfileCreationRequest;
import com.devteria.notification.dto.response.UserProfileResponse;
import com.devteria.notification.service.UserProfileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InternalUserProfileController {
    UserProfileService userProfileService;

    @PostMapping(value = {"/internal", "/internal/*"})
    UserProfileResponse createProfile(@RequestBody ProfileCreationRequest request) {
        return userProfileService.createProfile(request);
    }
}
