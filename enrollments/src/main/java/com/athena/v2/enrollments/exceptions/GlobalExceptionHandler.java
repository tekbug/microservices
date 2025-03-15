package com.athena.v2.enrollments.exceptions;

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

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ErrorResponseRecord> handleUnauthorizedAccessException(UnauthorizedAccessException ex, WebRequest request) {
        log.error("Unauthorized Access Exception", ex);
        return buildErrorResponse(ex, HttpStatus.UNAUTHORIZED, request);
    }

    @ExceptionHandler(EnrollmentAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseRecord> handleStudentAlreadyExistsException(EnrollmentAlreadyExistsException ex, WebRequest request) {
        log.error("Enrollment already exists: ", ex);
        return buildErrorResponse(ex, HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(EnrollmentNotFoundException.class)
    public ResponseEntity<ErrorResponseRecord> handleStudentNotFoundException(EnrollmentNotFoundException ex, WebRequest request) {
        log.error("Enrollment not found: ", ex);
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, request);
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
