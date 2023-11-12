package com.food.ordering.system.payment.service.domain;

import com.food.ordering.system.domain.valueobject.CustomerId;
import com.food.ordering.system.domain.valueobject.PaymentStatus;
import com.food.ordering.system.payment.service.domain.dto.PaymentRequest;
import com.food.ordering.system.payment.service.domain.entity.*;
import com.food.ordering.system.payment.service.domain.event.PaymentEvent;
import com.food.ordering.system.payment.service.domain.exception.PaymentApplicationPaymentException;
import com.food.ordering.system.payment.service.domain.mapper.PaymentDataMapper;
import com.food.ordering.system.payment.service.domain.ports.output.message.publisher.*;
import com.food.ordering.system.payment.service.domain.ports.output.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentRequestHelper {
    private final PaymentDomainService paymentDomainService;
    private final PaymentDataMapper paymentDataMapper;
    private final PaymentRepository paymentRepository;
    private final CreditEntryRepository creditEntryRepository;
    private final CreditHistoryRepository creditHistoryRepository;
    private final PaymentCompletedMessagePublisher paymentCompletedMessagePublisher;
    private final PaymentFailedMessagePublisher paymentFailedMessagePublisher;
    private final PaymentCancelledMessagePublisher paymentCancelledMessagePublisher;

    @Transactional
    public PaymentEvent persistPayment(PaymentRequest paymentRequest) {
        log.info("Received payment complete event for order id {}", paymentRequest.getOrderId());
        Payment payment = paymentDataMapper.paymentRequestModelToPayment(paymentRequest);
        return getPaymentEvent(payment);
    }

    @Transactional
    public PaymentEvent persistCancelPayment(PaymentRequest paymentRequest) {
        log.info("Received payment rollback event for order id {}", paymentRequest.getOrderId());
        Payment payment = paymentRepository.findById(UUID.fromString(paymentRequest.getOrderId()))
                .orElseThrow(() -> {
                    log.info("Payment with order id: {} could not be found!", paymentRequest.getOrderId());
                    return new PaymentApplicationPaymentException("Payment with order id: " +
                                                                  paymentRequest.getOrderId() + " could not be found!");
                });
        return getPaymentEvent(payment);
    }

    private PaymentEvent getPaymentEvent(Payment payment) {
        CreditEntry creditEntry = getCreditEntry(payment.getCustomerId());
        List<CreditHistory> creditHistories = getCreditHistory(payment.getCustomerId());
        List<String> failureMessages = new ArrayList<>();
        PaymentEvent paymentEvent = validateAndProcessPayment(payment, creditEntry, creditHistories, failureMessages);
        persistDbObjects(payment, creditEntry, creditHistories, failureMessages);
        return paymentEvent;
    }

    private PaymentEvent validateAndProcessPayment(Payment payment,
                                                   CreditEntry creditEntry,
                                                   List<CreditHistory> creditHistories,
                                                   List<String> failureMessages) {
        return payment.getPaymentStatus() == PaymentStatus.CANCELLED
                ? paymentDomainService.validateAndCancelPayment(payment, creditEntry, creditHistories, failureMessages,
                paymentCancelledMessagePublisher, paymentFailedMessagePublisher)
                : paymentDomainService.validateAndInitiatePayment(payment, creditEntry, creditHistories, failureMessages,
                paymentCompletedMessagePublisher, paymentFailedMessagePublisher);
    }

    private List<CreditHistory> getCreditHistory(CustomerId customerId) {
        return creditHistoryRepository.findByCustomerId(customerId).orElseThrow(() -> {
            log.error("Could not find credit history for customer: {}", customerId.getValue());
            return new PaymentApplicationPaymentException("Could not find credit history for customer: " +
                                                          customerId.getValue());
        });
    }

    private CreditEntry getCreditEntry(CustomerId customerId) {
        return creditEntryRepository.findByCustomerId(customerId).orElseThrow(() -> {
            log.error("Could not find credit entry for customer: {}", customerId.getValue());
            return new PaymentApplicationPaymentException("Could not find credit entry for customer: " +
                                                          customerId.getValue());
        });
    }

    private void persistDbObjects(Payment payment,
                                  CreditEntry creditEntry,
                                  List<CreditHistory> creditHistories,
                                  List<String> failureMessages) {
        paymentRepository.save(payment);
        if (failureMessages.isEmpty()) {
            creditEntryRepository.save(creditEntry);
            creditHistoryRepository.save(creditHistories.get(creditHistories.size() - 1));
        }
    }
}
