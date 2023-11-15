package com.food.ordering.system.restaurant.service.domain.entity;

import com.food.ordering.system.domain.entity.BaseEntity;
import com.food.ordering.system.domain.valueobject.Money;
import com.food.ordering.system.domain.valueobject.ProductId;
import lombok.Getter;

@Getter
public class Product extends BaseEntity<ProductId> {
    private String name;
    private Money price;
    private final int quantity;
    private boolean available;

    public void updateWithConfirmedNamePriceAndAvailability(String name, Money price, boolean available) {
        this.name = name;
        this.price = price;
        this.available = available;
    }

    Product(ProductId productId, String name, Money price, int quantity, boolean available) {
        super.setId(productId);
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.available = available;
    }

    public static ProductBuilder builder() {
        return new ProductBuilder();
    }

    public static class ProductBuilder {
        private ProductId productId;
        private String name;
        private Money price;
        private int quantity;
        private boolean available;

        ProductBuilder() {
        }

        public ProductBuilder productId(ProductId productId) {
            this.productId = productId;
            return this;
        }

        public ProductBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ProductBuilder price(Money price) {
            this.price = price;
            return this;
        }

        public ProductBuilder quantity(int quantity) {
            this.quantity = quantity;
            return this;
        }

        public ProductBuilder available(boolean available) {
            this.available = available;
            return this;
        }

        public Product build() {
            return new Product(this.productId, this.name, this.price, this.quantity, this.available);
        }

        public String toString() {
            return "Product.ProductBuilder(name=" + this.name + ", price=" + this.price + ", quantity=" + this.quantity + ", available=" + this.available + ")";
        }
    }
}
