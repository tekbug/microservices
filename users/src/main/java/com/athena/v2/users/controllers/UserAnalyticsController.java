package com.athena.v2.users.controllers;

import com.athena.v2.users.services.UsersAnalyticsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v2/users")
@RequiredArgsConstructor
@EnableMethodSecurity
@PreAuthorize("hasAnyRole('ADMIN, SUPER_ADMIN')")
@Tag(name = "User Analytics Controller", description = """
            Endpoints that are being used to manage users analytics within the system.
            The endpoints are only accessible by users with role of 'ADMIN' or 'SUPER_ADMIN' since
            the analytics are solely used for admin/super-admin dashboard.
        """)
public class UserAnalyticsController {

    private final UsersAnalyticsService analyticsService;

    @GetMapping
    public ResponseEntity<String> getUserCount() {
        return ResponseEntity.ok("Something");
    }

}
