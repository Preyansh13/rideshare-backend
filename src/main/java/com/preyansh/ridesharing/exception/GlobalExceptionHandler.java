package com.preyansh.ridesharing.exception;

import com.preyansh.ridesharing.model.Driver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import com.preyansh.ridesharing.dto.ValidationErrorResponse;
import com.preyansh.ridesharing.dto.ErrorResponse;

import java.util.Map;
import java.util.stream.Collectors;

// Applies centralized exception handling across REST controllers.
// Exceptions handled here can be returned as structured HTTP responses.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handles request-body validation failures, such as @NotBlank,
    // @Email, or @Pattern constraints failing in a DTO.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        // Extracts field names and their corresponding validation messages.
        // If multiple errors occur for the same field, keeps the first message.
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        error -> error.getField(),
                        error -> error.getDefaultMessage(),
                        (message1, message2) -> message1
                ));
        // Creates a structured response containing the status, field errors,
        // timestamp, and request path.
        ValidationErrorResponse errorResponse = new ValidationErrorResponse(HttpStatus.BAD_REQUEST.value(), errors, request.getRequestURI());
        // Returns HTTP 400 Bad Request with the validation error details.
        return ResponseEntity
                .badRequest()
                .body(errorResponse);
    }

    // Handles attempts to create a user or resource that already exists.
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(UserAlreadyExistsException ex, HttpServletRequest request) {
        // Uses HTTP 409 Conflict and the exception's message.
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.CONFLICT.value(), ex.getMessage(), request.getRequestURI());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(errorResponse);
    }

    // Handles requests for users that cannot be found.
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException ex, HttpServletRequest request) {
        // Returns HTTP 404 Not Found with the exception's message.
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage(), request.getRequestURI());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    // Handles invalid arguments, such as a missing or blank name search parameter.
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleEmptyNameParameter(IllegalArgumentException ex, HttpServletRequest request) {
        // Returns HTTP 400 Bad Request with the exception's message.
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), request.getRequestURI());
        return ResponseEntity
                .badRequest()
                .body(errorResponse);
    }

    // Handles database integrity violations, such as a duplicate value
    // violating a unique constraint.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex, HttpServletRequest request) {
        // Returns HTTP 409 Conflict with a generic email-conflict message.
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.CONFLICT.value(), "Email already exists", request.getRequestURI());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(errorResponse);
    }

    // Handles requests for vehicles that cannot be found.
    @ExceptionHandler(VehicleNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleVehicleNotFoundException(VehicleNotFoundException ex, HttpServletRequest request) {
        // Returns HTTP 404 Not Found with the exception's message.
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage(), request.getRequestURI());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    // Handles requests for rides that cannot be found.
    @ExceptionHandler(RideNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRideNotFoundException(RideNotFoundException ex, HttpServletRequest request) {
        // Returns HTTP 404 Not Found with the exception's message.
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage(), request.getRequestURI());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    // Handles attempts to perform a ride operation that is not allowed under the ride's current status.
    @ExceptionHandler(InvalidRideStatusException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRideStatusException(InvalidRideStatusException ex, HttpServletRequest request) {
        // Creates a structured error response with HTTP 409 Conflict, the exception message, and the request path.
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.CONFLICT.value(), ex.getMessage(), request.getRequestURI());
        // Returns HTTP 409 Conflict because the requested operation conflicts with the ride's current state.
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(errorResponse);
    }

    // Handles requests for drivers that do not exist in the database.
    // Returns HTTP 404 Not Found with the exception message and request path.
    @ExceptionHandler(DriverNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDriverNotFoundException(DriverNotFoundException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage(), request.getRequestURI());
        // Returns HTTP 404 Not Found with the structured error response.
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    // Handles ride-acceptance requests when a driver is not available.
    // Returns HTTP 409 Conflict because the operation conflicts with the driver's current status.
    @ExceptionHandler(InvalidDriverStatusException.class)
    public ResponseEntity<ErrorResponse> handleInvalidDriverStatusException(InvalidDriverStatusException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.CONFLICT.value(), ex.getMessage(), request.getRequestURI());
        // Returns HTTP 409 Conflict with the structured error response.
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(errorResponse);
    }
}