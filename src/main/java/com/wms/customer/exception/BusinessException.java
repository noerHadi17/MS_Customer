package com.wms.customer.exception;

import lombok.Getter;

/**
 * Base runtime exception for domain validation errors, carrying an i18n message code.
 */
@Getter
public class BusinessException extends RuntimeException {
    private final String messageCode;

    public BusinessException(String messageCode) {
        super(messageCode);
        this.messageCode = messageCode;
    }

    public BusinessException(String messageCode, String message) {
        super(message);
        this.messageCode = messageCode;
    }
}
