package com.S_Health.GenderHealthCare.exception;

import com.S_Health.GenderHealthCare.dto.ApiRespone;
import com.S_Health.GenderHealthCare.exception.exceptions.AppException;
import com.S_Health.GenderHealthCare.exception.exceptions.AppsException;
import com.S_Health.GenderHealthCare.exception.exceptions.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
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


    @ExceptionHandler(AppException.class)
    public ResponseEntity<String> handleAuthenticationException(AppException exception){
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.UNAUTHORIZED);
    }



    //lỗi 400
    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            IllegalArgumentException.class,
            BindException.class,
            TypeMismatchException.class
    })
    public ResponseEntity<ApiRespone> handleBadRequest(Exception e) {
        log.error("Bad request error: ", e);
        ApiRespone response = new ApiRespone();
        response.setCode(ErrorCode.BAD_REQUEST.getCode());
        response.setMessage(ErrorCode.BAD_REQUEST.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    // lỗi 500
    @ExceptionHandler({
            NullPointerException.class,
            IllegalStateException.class,
            ArithmeticException.class,
            ClassCastException.class,
            SQLException.class,
            RuntimeException.class // nếu không muốn bắt Runtime chung thì bỏ ra
    })
    public ResponseEntity<ApiRespone> handleServerError(Exception e) {
        log.error("Server error: ", e);
        ApiRespone response = new ApiRespone();
        response.setCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode());
        response.setMessage(ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiRespone> handleUnknownError(Exception e) {
        log.error("Unknown error: ", e);
        ApiRespone response = new ApiRespone();
        response.setCode(ErrorCode.UNKNOWN_ERROR.getCode());
        response.setMessage(ErrorCode.UNKNOWN_ERROR.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(value = AppsException.class)
    ResponseEntity<ApiRespone> handleAppException(AppsException e) {
        ErrorCode errorCode = e.getErrorCode();
        ApiRespone apiRespone = new ApiRespone();
        apiRespone.setCode(errorCode.getCode());
        apiRespone.setMessage(errorCode.getMessage());
        return ResponseEntity.badRequest().body(apiRespone);
    }
}
