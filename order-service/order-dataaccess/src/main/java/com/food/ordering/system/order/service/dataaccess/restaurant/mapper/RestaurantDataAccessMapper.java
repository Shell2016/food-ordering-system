package com.food.ordering.system.order.service.dataaccess.restaurant.mapper;

import com.food.ordering.system.dataaccess.restaurant.entity.RestaurantEntity;
import com.food.ordering.system.dataaccess.restaurant.exception.RestaurantDataAccessException;
import com.food.ordering.system.domain.valueobject.*;
import com.food.ordering.system.order.service.domain.entity.Product;
import com.food.ordering.system.order.service.domain.entity.Restaurant;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class RestaurantDataAccessMapper {

    public List<UUID> restaurantToRestaurantProducts(Restaurant restaurant) {
        return restaurant.getProducts().stream()
                .map(product -> product.getId().getValue())
                .toList();
    }

    public Restaurant restaurantEntitiesToRestaurant(List<RestaurantEntity> restaurantEntities) {
        RestaurantEntity entity = restaurantEntities.stream()
                .findFirst().orElseThrow(() -> new RestaurantDataAccessException("Restaurant could not be found!"));
        List<Product> products = restaurantEntities.stream()
                .map(restaurantEntity -> new Product(
                        new ProductId(restaurantEntity.getProductId()),
                        restaurantEntity.getProductName(),
                        new Money(restaurantEntity.getProductPrice())))
                .toList();
        return Restaurant.builder()
                .restaurantId(new RestaurantId(entity.getRestaurantId()))
                .products(products)
                .active(entity.isRestaurantActive())
                .build();
    }
}
