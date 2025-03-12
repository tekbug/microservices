package com.athena.v2.users.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.List;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(UserAlreadyExistException.class)
    public ResponseEntity<ErrorResponseRecord> handleUserAlreadyExistException(UserAlreadyExistException ex, WebRequest request) {
        log.error("User already exists. See log here: ", ex);
        return buildErrorResponse(ex, HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseRecord> handleUserNotFoundException(UserNotFoundException ex, WebRequest request) {
        log.error("User is not found, See log here: ", ex);
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<ErrorResponseRecord> handleRoleNotFoundException(RoleNotFoundException ex, WebRequest request) {
        log.error("Specified role is not found. See full log here: ", ex);
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(KeycloakRegistrationIntegrationErrorException.class)
    public ResponseEntity<ErrorResponseRecord> handleKeycloakError(KeycloakRegistrationIntegrationErrorException ex, WebRequest request) {
        log.error("External service error occurred with keycloak: ", ex);
        return buildErrorResponse(ex, HttpStatus.UNPROCESSABLE_ENTITY, request);
    }

    @ExceptionHandler(KeycloakSessionRetrievalException.class)
    public ResponseEntity<ErrorResponseRecord> handleKeycloakSessionRetrievalException(KeycloakSessionRetrievalException ex, WebRequest request) {
        log.error("External service error occurred with keycloak: ", ex);
        return buildErrorResponse(ex, HttpStatus.UNPROCESSABLE_ENTITY, request);
    }

    @ExceptionHandler(UnableToFetchNotificationException.class)
    public ResponseEntity<ErrorResponseRecord> handleUnableToFetchNotificationException(UnableToFetchNotificationException ex, WebRequest request) {
        log.error("Unable to fetch notification. See log here: ", ex);
        return buildErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler(InvalidUserStatusException.class)
    public ResponseEntity<ErrorResponseRecord> handleInvalidUserStatusException(InvalidUserStatusException ex, WebRequest request) {
        log.error("Invalid user status. See log here: ", ex);
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ErrorResponseRecord> handleUnauthorizedAccessException(UnauthorizedAccessException ex, WebRequest request) {
        log.error("Unauthorized access. See log here: ", ex);
        return buildErrorResponse(ex, HttpStatus.UNAUTHORIZED, request);
    }

    private ResponseEntity<ErrorResponseRecord> buildErrorResponse(Exception ex, HttpStatus status, WebRequest request) {
        ErrorResponseRecord responseRecord = ErrorResponseRecord.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(List.of(ex.getMessage()))
                .path(request.getDescription(false))
                .build();
        return new ResponseEntity<>(responseRecord, status);
    }

}
