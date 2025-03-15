package com.athena.v2.assignments.configs;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI openApiConfigurationForCourses() {
        return new OpenAPI()
                .info(new Info()
                        .title("Athena Courses Management API Documentation")
                        .version("2.0")
                        .description("""
                              This API provides a comprehensive set of endpoints for managing users within the system.
                              It handles all operations related to user accounts, profiles, authentication, and authorization.

                                            **Key Functionalities:**

                                            * **User Account Management:**
                                                * Create new user accounts with various attributes.
                                                * Retrieve user details by ID, username, or email.
                                                * Update user profile information (e.g., name, email, password, roles).
                                                * Delete user accounts (soft or hard delete).
                                                * List and filter users based on different criteria (pagination supported).
                                            * **User Profile Management:**
                                                * Manage user profile details beyond basic account information.
                                                * Store and retrieve user preferences and settings.
                                                * Handle user avatars or profile pictures.
                                                * Manage user activity logs (if applicable).
                                            * **User Search:**
                                                * Efficiently search for users based on keywords or specific fields.

                                            **This service acts as the single source of truth for all user-related data and operations within the application.**

                                            **Authentication:**

                                            All endpoints (unless explicitly stated otherwise) may require authentication.
                                            Please refer to the "Security Schemes" section for details on supported authentication methods.

                                            **Error Handling:**

                                            The API uses standard HTTP status codes to indicate the outcome of requests.
                                            Detailed error responses are provided in JSON format to help with debugging.

                                            **Rate Limiting and Throttling:**

                                            To ensure stability and prevent abuse, rate limiting and throttling may be implemented.
                                            Please refer to the specific endpoint documentation for details.
                              \s""")
                );
    }
}