package com.devteria.profile.dto.response;

import java.time.LocalDate;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PUBLIC)
public class UserProfileResponse {
    String firstName;
    String lastName;
    LocalDate dob;
    String city;
}
