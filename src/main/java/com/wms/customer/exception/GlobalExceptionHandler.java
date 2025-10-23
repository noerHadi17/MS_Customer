package com.wms.customer.exception;

import com.wms.customer.web.ApiResponseUtil;
import com.wms.customer.web.ResponseWrapper;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;

/**
 * Consolidates exception handling for the customer service, translating errors into consistent API responses.
 */
@org.springframework.web.bind.annotation.RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final MessageSource messageSource;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseWrapper<Void>> handleValidation(MethodArgumentNotValidException ex, Locale locale) {
        List<String> messages = ex.getBindingResult().getAllErrors().stream()
                .map(err -> {
                    if (err instanceof FieldError fe) {
                        return fe.getField() + ": " + (fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalid");
                    }
                    return err.getDefaultMessage() != null ? err.getDefaultMessage() : "invalid";
                })
                .collect(Collectors.toList());
        return ResponseEntity.badRequest().body(ApiResponseUtil.validationMessages(messages));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ResponseWrapper<Void>> handleConstraint(ConstraintViolationException ex, Locale locale) {
        List<String> messages = ex.getConstraintViolations().stream()
                .map(v -> v.getMessage())
                .collect(Collectors.toList());
        return ResponseEntity.badRequest().body(ApiResponseUtil.validationMessages(messages));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ResponseWrapper<Void>> handleBusiness(BusinessException ex, Locale locale) {
        String msg = messageSource.getMessage(ex.getMessageCode(), null, ex.getMessageCode(), locale);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponseUtil.error(ex.getMessageCode(), msg));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseWrapper<Void>> handleOther(Exception ex, Locale locale) {
        String msg = messageSource.getMessage("INTERNAL_ERROR", null, "Unexpected error", locale);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponseUtil.error("INTERNAL_ERROR", msg));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ResponseWrapper<Void>> handleDataIntegrity(DataIntegrityViolationException ex, Locale locale) {
        String root = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        org.slf4j.LoggerFactory.getLogger(getClass()).error("Data integrity error: {}", root);
        // Heuristic: if duplicate email constraint
        String code = (root != null && root.toLowerCase().contains("email")) ? "EMAIL_ALREADY_EXISTS" : "REGISTER_FAILURE";
        String msg = messageSource.getMessage(code, null, code, locale);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponseUtil.error(code, msg));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ResponseWrapper<Void>> handleNotReadable(HttpMessageNotReadableException ex, Locale locale) {
        // include short cause message to help FE/dev spot bad field format (e.g., date)
        String cause = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        String msg = messageSource.getMessage("INTERNAL_ERROR", null, "Unexpected error", locale);
        org.slf4j.LoggerFactory.getLogger(getClass()).warn("Request parse error: {}", cause);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponseUtil.error("INTERNAL_ERROR", msg));
    }
}
