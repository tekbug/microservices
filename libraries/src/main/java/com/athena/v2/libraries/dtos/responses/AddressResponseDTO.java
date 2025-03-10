package com.athena.v2.libraries.dtos.responses;

import lombok.Builder;
import lombok.NonNull;

@Builder
public record AddressResponseDTO(
        @NonNull String city,
        @NonNull String subCity,
        @NonNull String postalCode,
        @NonNull String streetName,
        @NonNull String houseNo
) {
}
