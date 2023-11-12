package com.food.ordering.system.payment.service.domain.exception;

import com.food.ordering.system.domain.exception.DomainException;

public class PaymentApplicationPaymentException extends DomainException {
    public PaymentApplicationPaymentException(String message) {
        super(message);
    }

    public PaymentApplicationPaymentException(String message, Throwable cause) {
        super(message, cause);
    }
}
