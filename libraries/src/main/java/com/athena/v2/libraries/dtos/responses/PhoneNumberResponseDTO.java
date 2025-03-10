package com.athena.v2.libraries.dtos.responses;

import lombok.Builder;
import lombok.NonNull;

@Builder
public record PhoneNumberResponseDTO(@NonNull String phoneNumber) {}
