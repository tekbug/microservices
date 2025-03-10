package com.athena.v2.libraries.dtos.responses;

import lombok.Builder;

import java.util.List;

@Builder
public record UserIdResponseDTO(String email, String username, List<String> roles, boolean isSuspended) {}
