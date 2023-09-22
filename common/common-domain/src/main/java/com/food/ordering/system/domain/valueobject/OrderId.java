package com.food.ordering.system.domain.valueobject;

import java.util.*;

public class OrderId extends BaseId<UUID> {
    protected OrderId(UUID value) {
        super(value);
    }
}
