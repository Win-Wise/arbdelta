package com.arbriver.arbdelta.lib.model.apimodel;

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
    private List<Bet> bets;
    private List<WinWiseResponse.Book> books;
    private List<String> errors;
    private List<Result> result_table;

    public record Bet(
            double wager,
            String bet_type,
            String value,
            double odds,
            String bookmaker,
            boolean lay,
            double volume,
            double previous_wager
    ) {}

    public record Book(
            String bookmaker,
            double commission,
            double wager_limit,
            boolean ignore_wager_precision,
            int max_wager_count
    ) {}

    public record Result(
            double profit,
            List<String> result
    ) {}
}


