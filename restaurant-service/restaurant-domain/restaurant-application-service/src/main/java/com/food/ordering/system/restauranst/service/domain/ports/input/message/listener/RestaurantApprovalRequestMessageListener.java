package com.food.ordering.system.restauranst.service.domain.ports.input.message.listener;

import com.food.ordering.system.restauranst.service.domain.dto.RestaurantApprovalRequest;

public interface RestaurantApprovalRequestMessageListener {
    void approveOrder(RestaurantApprovalRequest restaurantApprovalRequest);
}
