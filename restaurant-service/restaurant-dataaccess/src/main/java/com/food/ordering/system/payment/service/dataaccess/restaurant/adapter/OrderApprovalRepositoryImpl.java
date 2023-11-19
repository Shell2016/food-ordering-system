package com.food.ordering.system.payment.service.dataaccess.restaurant.adapter;

import com.food.ordering.system.payment.service.dataaccess.restaurant.mapper.RestaurantDataAccessMapper;
import com.food.ordering.system.payment.service.dataaccess.restaurant.repository.OrderApprovalJpaRepository;
import com.food.ordering.system.restauranst.service.domain.ports.output.repository.OrderApprovalRepository;
import com.food.ordering.system.restaurant.service.domain.entity.OrderApproval;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Repository;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class OrderApprovalRepositoryImpl implements OrderApprovalRepository {

    OrderApprovalJpaRepository orderApprovalJpaRepository;
    RestaurantDataAccessMapper restaurantDataAccessMapper;

    @Override
    public OrderApproval save(OrderApproval orderApproval) {
        return restaurantDataAccessMapper
                .orderApprovalEntityToOrderApproval(orderApprovalJpaRepository
                        .save(restaurantDataAccessMapper.orderApprovalToOrderApprovalEntity(orderApproval)));
    }
}
