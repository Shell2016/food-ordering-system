package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.domain.event.EmptyEvent;
import com.food.ordering.system.domain.valueobject.OrderId;
import com.food.ordering.system.order.service.domain.dto.message.PaymentResponse;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.event.OrderPaidEvent;
import com.food.ordering.system.order.service.domain.exception.OrderNotFoundException;
import com.food.ordering.system.order.service.domain.port.output.message.publisher.restaurantapproval.OrderPaidRestaurantRequestMessagePublisher;
import com.food.ordering.system.order.service.domain.port.output.repository.OrderRepository;
import com.food.ordering.system.saga.SagaStep;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class OrderPaymentSaga implements SagaStep<PaymentResponse, OrderPaidEvent, EmptyEvent> {

    OrderDomainService orderDomainService;
    OrderRepository orderRepository;
    OrderPaidRestaurantRequestMessagePublisher orderPaidRestaurantRequestMessagePublisher;

    @Override
    @Transactional
    public OrderPaidEvent process(PaymentResponse paymentResponse) {
        log.info("Completing payment for order with id: {}", paymentResponse.getOrderId());
        Order order = findOrder(paymentResponse.getOrderId());
        OrderPaidEvent domainEvent = orderDomainService.payOrder(order, orderPaidRestaurantRequestMessagePublisher);
        orderRepository.save(order);
        log.info("Order with id {} is paid", order.getId().getValue());
        return domainEvent;
    }

    @Override
    @Transactional
    public EmptyEvent rollback(PaymentResponse paymentResponse) {
        log.info("Cancelling order with id: {}", paymentResponse.getOrderId());
        Order order = findOrder(paymentResponse.getOrderId());
        orderDomainService.cancelOrder(order, paymentResponse.getFailureMessages());
        orderRepository.save(order);
        log.info("Order with id: {} is cancelled!", order.getId().getValue());
        return EmptyEvent.INSTANCE;
    }

    private Order findOrder(String orderId) {
        return orderRepository.findById(new OrderId(UUID.fromString(orderId))).orElseThrow(() -> {
            log.info("Order with id {} could not be found!", orderId);
            return new OrderNotFoundException("Order with id " + orderId + " could not be found!");
        });
    }
}
