package com.athena.v2.libraries.dtos.requests;

import lombok.Builder;
import lombok.NonNull;

import java.util.List;

@Builder
public record BatchUserRequestDTO(
        @NonNull List<String> userIds,
        boolean includeContactInfo,
        boolean includeEnrollmentInfo
        ) {
}
