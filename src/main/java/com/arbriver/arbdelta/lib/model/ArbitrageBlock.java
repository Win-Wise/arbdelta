package com.arbriver.arbdelta.lib.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@Document(collection = "arbitrages")
public class ArbitrageBlock {
    @Id
    private String matching_key;
    private double last_best_profit;
    private double last_worst_profit;
    private String match_id;
    private Instant last_seen;
    private Instant first_seen;
    private List<Arbitrage> arbitrages;
}
