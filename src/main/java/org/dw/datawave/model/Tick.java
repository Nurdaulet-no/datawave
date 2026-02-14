package org.dw.datawave.model;

import java.time.Instant;

public record Tick(
        String symbol,
        Double price,
        Integer volume,
        Instant createdAt
) {

}
