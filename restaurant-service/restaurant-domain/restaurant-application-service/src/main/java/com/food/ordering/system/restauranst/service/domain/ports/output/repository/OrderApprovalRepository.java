package com.food.ordering.system.restauranst.service.domain.ports.output.repository;

import com.food.ordering.system.restaurant.service.domain.entity.OrderApproval;

public interface OrderApprovalRepository {
    OrderApproval save(OrderApproval orderApproval);
}
