package com.athena.v2.libraries.dtos.responses;

import com.athena.v2.libraries.enums.UserRoles;
import com.athena.v2.libraries.enums.UserStatus;
import lombok.Builder;
import lombok.NonNull;

import java.util.List;

@Builder
public record UserResponseDTO(
        @NonNull String userId,
        @NonNull String username,
        @NonNull String email,
        @NonNull String firstName,
        @NonNull String middleName,
        @NonNull String lastName,
        @NonNull List<AddressResponseDTO> addresses,
        @NonNull List<PhoneNumberResponseDTO> phoneNumbers,
        @NonNull UserStatus userStatus,
        @NonNull UserRoles userRoles
        ) {}
