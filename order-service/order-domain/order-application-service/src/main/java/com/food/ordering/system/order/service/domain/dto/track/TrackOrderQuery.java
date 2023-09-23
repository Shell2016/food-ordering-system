package com.food.ordering.system.order.service.domain.dto.track;

import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Getter
@Builder
public class TrackOrderQuery {
    @NotNull
    private final UUID orderTrackingId;
}
