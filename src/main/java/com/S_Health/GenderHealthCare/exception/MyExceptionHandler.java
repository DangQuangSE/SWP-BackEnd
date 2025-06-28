package com.S_Health.GenderHealthCare.exception;

import com.S_Health.GenderHealthCare.exception.exceptions.AppException;
import com.S_Health.GenderHealthCare.exception.exceptions.AuthenticationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class MyExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleBadRequestException(MethodArgumentNotValidException exception) {

        // Lấy tên class đang bị validate để xác định thứ tự field tương ứng
        String targetClass = exception.getBindingResult().getTarget().getClass().getSimpleName();

        List<String> fieldOrder;
        switch (targetClass) {
            case "RegisterRequestStep1" -> fieldOrder = List.of("phone", "password", "confirmPassword");
            case "RegisterRequestStep2" -> fieldOrder = List.of("fullname", "email", "dateOfBirth", "gender");
            default -> fieldOrder = List.of(); // fallback
        }

        String responseMessage = exception.getFieldErrors().stream()
                .sorted(Comparator.comparingInt(e -> fieldOrder.indexOf(e.getField())))
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("\n"));

        return new ResponseEntity<>(responseMessage, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity handleAuthenticationException(AuthenticationException exception){
        return new ResponseEntity(exception.getMessage(), HttpStatus.UNAUTHORIZED);
    }
    @ExceptionHandler(AppException.class)
    public ResponseEntity handleAuthenticationException(AppException exception){
        return new ResponseEntity(exception.getMessage(), HttpStatus.UNAUTHORIZED);
    }
}
