package org.dw.datawave.dto;

import java.time.Instant;

public record TimeRange(
        Instant from,
        Instant to
) {
}
