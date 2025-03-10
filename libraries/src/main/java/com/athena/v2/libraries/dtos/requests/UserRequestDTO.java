package com.athena.v2.libraries.dtos.requests;

import com.athena.v2.libraries.enums.UserRoles;
import com.athena.v2.libraries.enums.UserStatus;
import lombok.Builder;
import lombok.NonNull;

import java.util.List;

@Builder
public record UserRequestDTO(
        @NonNull String username,
        @NonNull String password,
        @NonNull String email,
        @NonNull String firstName,
        @NonNull String middleName,
        @NonNull String lastName,
        @NonNull List<PhoneNumberRequestDTO> phoneNumbers,
        @NonNull List<AddressRequestDTO> addresses,
        @NonNull UserStatus userStatus,
        @NonNull UserRoles userRoles
) {}

