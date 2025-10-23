package com.wms.customer.i18n;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;

/**
 * Enumerates message codes and default HTTP statuses used throughout the customer service.
 */
@Getter
@AllArgsConstructor
public enum I18nMessageCollection {
    REGISTER_SUCCESS(HttpStatus.OK.value(), "REGISTER_SUCCESS"),
    REGISTER_FAILURE(HttpStatus.BAD_REQUEST.value(), "REGISTER_FAILURE"),
    EMAIL_ALREADY_EXISTS(HttpStatus.BAD_REQUEST.value(), "EMAIL_ALREADY_EXISTS"),
    LOGIN_SUCCESS(HttpStatus.OK.value(), "LOGIN_SUCCESS"),
    LOGIN_FAILURE(HttpStatus.BAD_REQUEST.value(), "LOGIN_FAILURE"),
    AUTH_INVALID_CREDENTIALS(HttpStatus.BAD_REQUEST.value(), "AUTH_INVALID_CREDENTIALS"),
    CHANGE_PASSWORD_SUCCESS(HttpStatus.OK.value(), "CHANGE_PASSWORD_SUCCESS"),
    KYC_SUBMIT_SUCCESS(HttpStatus.OK.value(), "KYC_SUBMIT_SUCCESS"),
    KYC_VALIDATION_FAILED(HttpStatus.BAD_REQUEST.value(), "KYC_VALIDATION_FAILED"),
    EMAIL_CHECKED(HttpStatus.OK.value(), "EMAIL_CHECKED"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "INTERNAL_ERROR");

    private final Integer statusCode;
    private final String i18nMessage;

    public String localized(MessageSource ms, java.util.Locale locale) {
        return ms.getMessage(i18nMessage, null, locale);
    }
}
