package com.arbriver.arbdelta.lib.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Builder
@Getter
@Setter
public class Arbitrage {
    private final String matching_key;
    private final String match_id;
    @Builder.Default
    private final double best_profit = 0.0;
    @Builder.Default
    private final double worst_profit = 0.0;
    private final Instant timestamp;
    private final List<BookPosition> portfolio;
}
