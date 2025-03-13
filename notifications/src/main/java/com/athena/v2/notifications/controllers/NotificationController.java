package com.athena.v2.notifications.controllers;

import com.athena.v2.libraries.dtos.responses.NotificationResponseDTO;
import com.athena.v2.notifications.services.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v2/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification Controller", description = """
            Endpoints that are being used to manage notifications within the system.
            The endpoints are accessible by all users in the system.
        """)
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/get-notifications")
    @Operation(summary = "Get All Notifications for the current user", description =
            """
            Retrieve a list of all notifications, optionally filtered by status.
            This endpoint returns a simple list of notifications.
            """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of notifications.",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = NotificationResponseDTO.class)))),
            @ApiResponse(responseCode = "500", description = "Internal server error.",
                    content = @Content(mediaType = "application/json", schema = @Schema(description = "Error response")))
    })
    public ResponseEntity<List<NotificationResponseDTO>> getAllNotifications() {
        return ResponseEntity.status(HttpStatus.OK).body(notificationService.getAllNotificationsForUser());
    }
}
