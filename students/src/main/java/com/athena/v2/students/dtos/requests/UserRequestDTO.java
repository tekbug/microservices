package com.athena.v2.students.dtos.requests;

import lombok.NonNull;

public record UserRequestDTO(@NonNull String userId, @NonNull String email) {
}
