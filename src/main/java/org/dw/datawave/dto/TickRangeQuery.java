package org.dw.datawave.dto;


public record TickRangeQuery(
        TimeRange range,
        int limit
) {
}
