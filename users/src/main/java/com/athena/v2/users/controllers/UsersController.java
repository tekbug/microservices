package com.athena.v2.users.controllers;

import com.athena.v2.libraries.dtos.requests.UserIdRequestDTO;
import com.athena.v2.libraries.dtos.requests.UserRequestDTO;
import com.athena.v2.libraries.dtos.responses.UserIdResponseDTO;
import com.athena.v2.libraries.dtos.responses.UserResponseDTO;
import com.athena.v2.users.annotations.CurrentUser;
import com.athena.v2.users.services.UsersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/v2/users")
@RequiredArgsConstructor
@Tag(name = "Users Controller", description = """
            Endpoints that are being used to manage users within the system.
            The endpoints are only accessible by users with role of 'ADMIN' or 'SUPER_ADMIN'
        """)
@EnableMethodSecurity
@PreAuthorize("hasRole('ADMINISTRATOR')")
public class UsersController {

    private final UsersService usersService;

    @GetMapping("/get-all-users")
    @Operation(summary = "Get All Users", description =
            """
            Retrieve a list of all users, optionally filtered by status.
            This endpoint returns a simple list of users.
            """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of users.",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = UserResponseDTO.class)))),
            @ApiResponse(responseCode = "500", description = "Internal server error.",
                    content = @Content(mediaType = "application/json", schema = @Schema(description = "Error response")))
    })
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.status(HttpStatus.OK).body(usersService.returnAllUsers());
    }

    @GetMapping("/get-all-users-by-roles/{role}")
    @Operation(summary = "Get All Users", description =
            """
            Retrieve a list of all users, optionally filtered by status.
            This endpoint returns a simple list of users.
            """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of users.",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = UserResponseDTO.class)))),
            @ApiResponse(responseCode = "500", description = "Internal server error.",
                    content = @Content(mediaType = "application/json", schema = @Schema(description = "Error response")))
    })
    public ResponseEntity<List<UserResponseDTO>> getAllUsersByRole(@PathVariable("role") String role) {
        return ResponseEntity.status(HttpStatus.OK).body(usersService.returnAllUsersByRole(role));
    }

    @GetMapping
    @Operation(summary = "Get User ID for Internal Usage", description =
            """
            Retrieve the `preferred_username` of the user from keycloak for internal usage.
            """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of user's `preferred_username`.",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = UserIdResponseDTO.class)))),
            @ApiResponse(responseCode = "500", description = "Internal server error.",
                    content = @Content(mediaType = "application/json", schema = @Schema(description = "Error response")))
    })
    public ResponseEntity<UserIdResponseDTO> getUser(@CurrentUser UserIdRequestDTO userIdRequestDTO) {
        return ResponseEntity.status(HttpStatus.OK).body(usersService.returnCurrentUserInformation(userIdRequestDTO));
    }

    @GetMapping("/get-user/{id}")
    @Operation(summary = "Get User by ID", description = "Retrieve a specific user by their unique ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of the users.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserResponseDTO.class, description = "User details"))),
            @ApiResponse(responseCode = "404", description = "User not found.",
                    content = @Content(mediaType = "application/json", schema = @Schema(description = "Error response"))),
            @ApiResponse(responseCode = "500", description = "Internal server error.",
                    content = @Content(mediaType = "application/json", schema = @Schema(description = "Error response")))
    })
    public ResponseEntity<UserResponseDTO> getUser(@Parameter(description = "Unique identifier for a User") @PathVariable String id) {
        return ResponseEntity.status(HttpStatus.OK).body(usersService.getUserById(id));
    }

    @GetMapping("/get-user-status/{statusCode}")
    @Operation(summary = "Get Users by Their Status", description = "Retrieve list of users by their status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of the user.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserResponseDTO.class, description = "User details"))),
            @ApiResponse(responseCode = "404", description = "User not found.",
                    content = @Content(mediaType = "application/json", schema = @Schema(description = "Error response"))),
            @ApiResponse(responseCode = "500", description = "Internal server error.",
                    content = @Content(mediaType = "application/json", schema = @Schema(description = "Error response")))
    })
    public ResponseEntity<List<UserResponseDTO>> getUsersByStatus(@Parameter(description = "Status identifier for filtering purposes") @PathVariable String statusCode) {
        return ResponseEntity.status(HttpStatus.OK).body(usersService.returnUserByUserStatus(statusCode));
    }

    @PostMapping("/register-user")
    @Operation(summary = "Register Users to the System", description = """
            Register users to the system, optionally registering users in batch.
            This endpoint returns a string message confirming the input is working as intended, or error message.
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successful creation of the user.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserResponseDTO.class, description = "User details"))),
            @ApiResponse(responseCode = "400", description = "Bad Request.",
                    content = @Content(mediaType = "application/json", schema = @Schema(description = "Error response"))),
            @ApiResponse(responseCode = "409", description = "Conflict.",
                    content = @Content(mediaType = "application/json", schema = @Schema(description = "Error response"))),
            @ApiResponse(responseCode = "500", description = "Internal server error.",
                    content = @Content(mediaType = "application/json", schema = @Schema(description = "Error response")))
    })
    public ResponseEntity<String> registerUser(@Parameter(description = "Validated request DTO body for mapping inputs to the database") @Valid @RequestBody UserRequestDTO userRequestDTO) {
        usersService.registerUser(userRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body("user is created successfully.");
    }

    @PutMapping("/update-user/{id}")
    @Operation(summary = "Update User by ID", description = "Update an existing user's information using their unique ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successful update of the user.",
                    content = @Content(mediaType = "application/json", schema = @Schema(description = "Success message"))),
            @ApiResponse(responseCode = "400", description = "Bad Request.",
                    content = @Content(mediaType = "application/json", schema = @Schema(description = "Error response"))),
            @ApiResponse(responseCode = "404", description = "User not found.",
                    content = @Content(mediaType = "application/json", schema = @Schema(description = "Error response"))),
            @ApiResponse(responseCode = "500", description = "Internal server error.",
                    content = @Content(mediaType = "application/json", schema = @Schema(description = "Error response")))
    })
    public ResponseEntity<String> updateUser(@Parameter(description = "Unique identifier for a User") @PathVariable String id, @RequestBody UserRequestDTO userRequestDTO) {
        usersService.updateUser(id, userRequestDTO);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("user is updated successfully.");
    }

    @PutMapping("/reinstate-user/{id}")
    @Operation(summary = "Reinstate User by ID", description = "Change the status of a user to ACTIVE using their unique ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successful reinstatement of the user.",
                    content = @Content(mediaType = "application/json", schema = @Schema(description = "Success message"))),
            @ApiResponse(responseCode = "404", description = "User not found.",
                    content = @Content(mediaType = "application/json", schema = @Schema(description = "Error response"))),
            @ApiResponse(responseCode = "500", description = "Internal server error.",
                    content = @Content(mediaType = "application/json", schema = @Schema(description = "Error response")))
    })
    public ResponseEntity<String> reinstateUser(@Parameter(description = "Unique identifier for a User") @PathVariable String id) {
        usersService.updateUserStatus(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("user is reinstated successfully.");
    }

    @PutMapping("/block-user/{id}")
    @Operation(summary = "Block User by ID", description = "Change the status of a user to BLOCKED using their unique ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successful blocking of the user.",
                    content = @Content(mediaType = "application/json", schema = @Schema(description = "Success message"))),
            @ApiResponse(responseCode = "404", description = "User not found.",
                    content = @Content(mediaType = "application/json", schema = @Schema(description = "Error response"))),
            @ApiResponse(responseCode = "500", description = "Internal server error.",
                    content = @Content(mediaType = "application/json", schema = @Schema(description = "Error response")))
    })
    public ResponseEntity<String> blockUser(@Parameter(description = "Unique identifier for a User") @PathVariable String id) {
        usersService.blockUser(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("user is blocked successfully.");
    }

    @DeleteMapping("/delete-user/{id}")
    @Operation(summary = "Delete User by ID", description = "Logically delete a user by setting their status to SUSPENDED using their unique ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successful deletion of the user.",
                    content = @Content(mediaType = "application/json", schema = @Schema(description = "Success message"))),
            @ApiResponse(responseCode = "404", description = "User not found.",
                    content = @Content(mediaType = "application/json", schema = @Schema(description = "Error response"))),
            @ApiResponse(responseCode = "500", description = "Internal server error.",
                    content = @Content(mediaType = "application/json", schema = @Schema(description = "Error response")))
    })
    public ResponseEntity<String> deleteUser(@Parameter(description = "Unique identifier for a User") @PathVariable String id) {
        usersService.deleteUserById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("user is deleted successfully.");
    }

    @PostMapping("exists")
    public ResponseEntity<Boolean> isUserExists(@RequestBody Map<String, String> requestBody) {
        String userId = requestBody.get("userId");
        String email = requestBody.get("email");
        return ResponseEntity.status(HttpStatus.OK).body(usersService.isUserExists(userId, email));
    }

    @PostMapping("exists-user")
    public ResponseEntity<Boolean> isUserExist(@RequestBody String userId) {
        return ResponseEntity.status(HttpStatus.OK).body(usersService.isUserExist(userId));
    }

    @GetMapping("get-email")
    public ResponseEntity<String> getUserEmail(@RequestBody String userId) {
        return ResponseEntity.status(HttpStatus.OK).body(usersService.getEmailForUserById(userId));
    }
}
