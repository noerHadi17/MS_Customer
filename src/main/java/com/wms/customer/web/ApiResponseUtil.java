package com.wms.customer.web;

import java.util.List;

public final class ApiResponseUtil {
    private ApiResponseUtil() {}

    public static <T> ResponseWrapper<T> success(T data, String messageCode, String message) {
        return ResponseWrapper.<T>builder()
                .success(true)
                .data(data)
                .messages(message != null ? List.of(message) : null)
                .messageCodes(messageCode != null ? List.of(messageCode) : null)
                .build();
    }

    public static ResponseWrapper<Void> error(String code, String message) {
        return ResponseWrapper.<Void>builder()
                .success(false)
                .data(null)
                .messages(message != null ? List.of(message) : null)
                .messageCodes(code != null ? List.of(code) : null)
                .build();
    }

    public static ResponseWrapper<Void> validationMessages(List<String> messages) {
        return ResponseWrapper.<Void>builder()
                .success(false)
                .data(null)
                .messages(messages)
                .messageCodes(null)
                .build();
    }
}
