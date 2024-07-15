package com.arbriver.arbdelta.lib.model;

import lombok.Data;

import java.util.List;

@Data
public class WinWiseResponse {
    private List<Double> profit;
    private double wager_limit;
    private double wager_precision;
    private boolean no_draw;
    private int mode;
    private int max_process_time;
    private int total_max_wager_count;
    private List<WinWiseBet> bets;
    private List<WinWiseBook> books;
    private List<String> errors;
    private List<WinWiseResult> result_table;

    public record WinWiseBet(
            double wager,
            String bet_type,
            String value,
            double odds,
            String bookmaker,
            boolean lay,
            double volume,
            double previous_wager
    ) {}

    public record WinWiseBook(
            String bookmaker,
            double commission,
            double wager_limit,
            boolean ignore_wager_precision,
            int max_wager_count
    ) {}

    public record WinWiseResult(
            double profit,
            List<String> result
    ) {}
}


