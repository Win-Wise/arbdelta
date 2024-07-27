package com.arbriver.arbdelta.lib.model.apimodel;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public final class WinWiseRequest {
    @Builder.Default
    private double wager_limit = -1;
    @Builder.Default
    private double wager_precision = 0.01;
    @Builder.Default
    private boolean no_draw = false;
    @Builder.Default
    private int mode = 1;
    @Builder.Default
    private int max_process_time = 25;
    @Builder.Default
    private int total_max_wager_count = -1;
    private String executionId;
    private List<WinWiseRequest.Bet> bets;
    private List<WinWiseRequest.Book> books;

    @Builder
    @Setter
    @Getter
    public final static class Bet {
        private final String bet_type;
        private final String value;
        private final double odds;
        private final String bookmaker;
        @Builder.Default
        private boolean lay = false;
        @Builder.Default
        private double volume = -1.0;
        @Builder.Default
        private double previous_wager = 0.0;
    }

    @Builder
    @Getter
    @Setter
    public static final class Book {
        private final String bookmaker;
        @Builder.Default
        private double commission = 0.0;
        @Builder.Default
        private double wager_limit = -1.0;
        @Builder.Default
        private boolean ignore_wager_precision = false;
        @Builder.Default
        private int max_wager_count = -1;
    }
}
