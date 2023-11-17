package com.food.ordering.system.restauranst.service.domain;

import com.food.ordering.system.domain.valueobject.OrderId;
import com.food.ordering.system.restauranst.service.domain.dto.RestaurantApprovalRequest;
import com.food.ordering.system.restauranst.service.domain.mapper.RestaurantDataMapper;
import com.food.ordering.system.restauranst.service.domain.ports.output.message.publisher.OrderApprovedMessagePublisher;
import com.food.ordering.system.restauranst.service.domain.ports.output.message.publisher.OrderRejectedMessagePublisher;
import com.food.ordering.system.restauranst.service.domain.ports.output.repository.OrderApprovalRepository;
import com.food.ordering.system.restauranst.service.domain.ports.output.repository.RestaurantRepository;
import com.food.ordering.system.restaurant.service.domain.RestaurantDomainService;
import com.food.ordering.system.restaurant.service.domain.entity.Restaurant;
import com.food.ordering.system.restaurant.service.domain.event.OrderApprovalEvent;
import com.food.ordering.system.restaurant.service.domain.exception.RestaurantNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RestaurantApprovalRequestHelper {

    RestaurantDomainService restaurantDomainService;
    RestaurantDataMapper restaurantDataMapper;
    OrderApprovalRepository orderApprovalRepository;
    RestaurantRepository restaurantRepository;
    OrderApprovedMessagePublisher orderApprovedMessagePublisher;
    OrderRejectedMessagePublisher orderRejectedMessagePublisher;

    @Transactional
    public OrderApprovalEvent persistOrderApproval(RestaurantApprovalRequest restaurantApprovalRequest) {
        log.info("Processing restaurant approval for order id: {}", restaurantApprovalRequest.getOrderId());
        List<String> failureMessages = new ArrayList<>();
        Restaurant restaurant = findRestaurant(restaurantApprovalRequest);
        var orderApprovalEvent = restaurantDomainService.validateOrder(
                restaurant,
                failureMessages,
                orderApprovedMessagePublisher,
                orderRejectedMessagePublisher
        );
        orderApprovalRepository.save(restaurant.getOrderApproval());
        return orderApprovalEvent;
    }

    private Restaurant findRestaurant(RestaurantApprovalRequest restaurantApprovalRequest) {
        var restaurant =
                restaurantDataMapper.restaurantApprovalRequestToRestaurant(restaurantApprovalRequest);
        var restaurantEntity = restaurantRepository.findRestaurantInformation(restaurant).orElseThrow(() -> {
            log.error("Restaurant with id {} was not found", restaurantApprovalRequest.getRestaurantId());
            return new RestaurantNotFoundException("Restaurant with id " + restaurantApprovalRequest.getRestaurantId() +
                                                   " was not found");
        });
        restaurant.setActive(restaurantEntity.isActive());
        restaurant.getOrderDetail().getProducts().forEach(product ->
            restaurantEntity.getOrderDetail().getProducts().forEach(p -> {
                if (p.getId().equals(product.getId())) {
                    product.updateWithConfirmedNamePriceAndAvailability(p.getName(), p.getPrice(), p.isAvailable());
                }
            })
        );
        restaurant.getOrderDetail().setId(new OrderId(UUID.fromString(restaurantApprovalRequest.getOrderId())));
        return restaurant;
    }
}
