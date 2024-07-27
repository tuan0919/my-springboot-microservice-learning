package com.devteria.profile.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PUBLIC)
public class ProfileCreationRequest {
    String firstName;
    String lastName;
    LocalDate dob;
    String city;
}
