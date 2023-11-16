package com.food.ordering.system.restaurant.service.domain.entity;

import com.food.ordering.system.domain.entity.AggregateRoot;
import com.food.ordering.system.domain.valueobject.*;
import com.food.ordering.system.restaurant.service.domain.valueobject.OrderApprovalId;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
public class Restaurant extends AggregateRoot<RestaurantId> {
    private OrderApproval orderApproval;
    @Setter
    private boolean active;
    private final OrderDetail orderDetail;

    private Restaurant(RestaurantId restaurantId, OrderApproval orderApproval, boolean active, OrderDetail orderDetail) {
        super.setId(restaurantId);
        this.orderApproval = orderApproval;
        this.active = active;
        this.orderDetail = orderDetail;
    }

    public void validateOrder(List<String> failureMessages) {
        if (orderDetail.getOrderStatus() != OrderStatus.PAID) {
            failureMessages.add("Payment is not completed for order: " + orderDetail.getId());
        }
        Money totalAmount = orderDetail.getProducts().stream()
                .map(product -> {
                    if (!product.isAvailable()) {
                        failureMessages.add("Product with id: " + product.getId().getValue() + " is not avaliable");
                    }
                    return product.getPrice().multiply(product.getQuantity());
                })
                .reduce(Money.ZERO, Money::add);
        if (!totalAmount.equals(orderDetail.getTotalAmount())) {
            failureMessages.add("Price total is not correct for order: " + orderDetail.getId());
        }
    }

    public void constructOrderApproval(OrderApprovalStatus orderApprovalStatus) {
        this.orderApproval = OrderApproval.builder()
                .orderApprovalId(new OrderApprovalId(UUID.randomUUID()))
                .restaurantId(this.getId())
                .orderId(this.getOrderDetail().getId())
                .orderApprovalStatus(orderApprovalStatus)
                .build();
    }

    public static RestaurantBuilder builder() {
        return new RestaurantBuilder();
    }

    public static class RestaurantBuilder {
        private RestaurantId restaurantId;
        private OrderApproval orderApproval;
        private boolean active;
        private OrderDetail orderDetail;

        RestaurantBuilder() {
        }

        public RestaurantBuilder restaurantId(RestaurantId restaurantId) {
            this.restaurantId = restaurantId;
            return this;
        }

        public RestaurantBuilder orderApproval(OrderApproval orderApproval) {
            this.orderApproval = orderApproval;
            return this;
        }

        public RestaurantBuilder active(boolean active) {
            this.active = active;
            return this;
        }

        public RestaurantBuilder orderDetail(OrderDetail orderDetail) {
            this.orderDetail = orderDetail;
            return this;
        }

        public Restaurant build() {
            return new Restaurant(this.restaurantId, this.orderApproval, this.active, this.orderDetail);
        }

        public String toString() {
            return "Restaurant.RestaurantBuilder(orderApproval=" + this.orderApproval + ", active=" + this.active + ", orderDetail=" + this.orderDetail + ")";
        }
    }
}
