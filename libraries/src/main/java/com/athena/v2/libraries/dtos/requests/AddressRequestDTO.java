package com.athena.v2.libraries.dtos.requests;

import lombok.Builder;
import lombok.NonNull;

@Builder
public record AddressRequestDTO(
        @NonNull String city,
        @NonNull String subCity,
        @NonNull String postalCode,
        @NonNull String streetName,
        @NonNull String houseNo
) {}
