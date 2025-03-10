package com.athena.v2.libraries.dtos.requests;

import lombok.Builder;

import java.util.List;

@Builder
public record UserIdRequestDTO(String userId, String username, List<String> roles, String email) {}
