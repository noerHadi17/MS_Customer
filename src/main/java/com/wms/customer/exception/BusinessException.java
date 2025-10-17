package com.wms.customer.exception;

import lombok.Getter;

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

