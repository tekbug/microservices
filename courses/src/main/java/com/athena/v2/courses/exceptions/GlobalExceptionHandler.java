package com.athena.v2.courses.exceptions;

import com.athena.v2.teachers.exceptions.UnauthorizedAccessException;
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

    @ExceptionHandler(TeacherAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseRecord> handleStudentAlreadyExistsException(TeacherAlreadyExistsException ex, WebRequest request) {
        log.error("Teacher already exists: ", ex);
        return buildErrorResponse(ex, HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(TeacherNotFoundException.class)
    public ResponseEntity<ErrorResponseRecord> handleStudentNotFoundException(TeacherNotFoundException ex, WebRequest request) {
        log.error("Teacher not found: ", ex);
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
