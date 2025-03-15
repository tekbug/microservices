package com.athena.v2.assignments.exceptions;

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

    @ExceptionHandler(AssignmentNotFoundException.class)
    public ResponseEntity<ErrorResponseRecord> handleAssignmentNotFoundException(AssignmentNotFoundException ex, WebRequest request) {
        log.error(ex.getMessage());
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ErrorResponseRecord> handleUnauthorizedAccessException(UnauthorizedAccessException ex, WebRequest request) {
        log.error(ex.getMessage());
        return buildErrorResponse(ex, HttpStatus.UNAUTHORIZED, request);
    }

    @ExceptionHandler(AssignmentAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseRecord> handleAssignmentAlreadyExistsException(AssignmentAlreadyExistsException ex, WebRequest request) {
        log.error(ex.getMessage());
        return buildErrorResponse(ex, HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(SubmissionNotFoundException.class)
    public ResponseEntity<ErrorResponseRecord> handleSubmissionNotFoundException(SubmissionNotFoundException ex, WebRequest request) {
        log.error(ex.getMessage());
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
