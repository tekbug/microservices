package com.athena.v2.libraries.dtos.audits;

import lombok.Builder;
import lombok.NonNull;

import java.time.LocalDateTime;

@Builder
public record AuditableDTO(
        @NonNull String createdBy,
        @NonNull LocalDateTime createdAt,
        @NonNull String lastModifiedBy,
        @NonNull LocalDateTime lastModifiedAt
        ) {
}
