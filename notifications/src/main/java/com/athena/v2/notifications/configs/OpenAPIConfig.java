package com.athena.v2.notifications.configs;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI apiConfiguration() {
        return new OpenAPI()
                .info(new Info()
                        .title("Athena Notification Management API Documentation")
                        .version("2.0")
                        .description("""
                              This API provides a comprehensive set of endpoints for managing notifications within the system.
                              It handles all operations related to notification retrieval, saving, event-driven mailing list and more.

                                            **Key Functionalities:**

                                            * **Notification Management:**
                                                * Create notifications with various attributes.
                                                * Retrieve notification details by user ID, eventId, and more.
                                                * List and filter notifications based on different criteria (pagination supported for web version).
                                            * **User Notification Management:**
                                                * Manage user notification details beyond basic account information.
                                                * Store and retrieve user preferences and settings.
                                                * Manage notification activity logs.
                                            * **Notification Search:**
                                                * Efficiently search for targeted or bulk notification that is based on keywords or specific fields.

                                            **This service acts as the single source of truth for all notification-related data and operations within the application.**

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