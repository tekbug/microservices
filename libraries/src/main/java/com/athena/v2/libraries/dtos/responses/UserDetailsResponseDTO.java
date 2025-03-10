package com.athena.v2.libraries.dtos.responses;

import com.athena.v2.libraries.enums.UserStatus;
import lombok.Builder;
import lombok.NonNull;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record UserDetailsResponseDTO(
        @NonNull String userId,
        @NonNull String fullName,
        @NonNull String email,
        @NonNull String phoneNumber,
        @NonNull List<String> enrolledCourses,
        @NonNull UserStatus status,
        LocalDateTime lastLogin
        ) {}
