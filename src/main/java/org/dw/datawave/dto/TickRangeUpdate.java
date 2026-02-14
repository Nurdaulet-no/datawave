package org.dw.datawave.dto;

public record TickRangeUpdate(
        TimeRange range,
        int delta
){
}
