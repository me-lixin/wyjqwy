package com.wyjqwy.server.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BizException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleBiz(BizException e) {
        return new ApiResponse<>(400, e.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValid(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldError() == null
                ? "invalid request"
                : e.getBindingResult().getFieldError().getDefaultMessage();
        return new ApiResponse<>(400, message, null);
    }
}
