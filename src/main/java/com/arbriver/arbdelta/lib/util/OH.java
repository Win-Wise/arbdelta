package com.arbriver.arbdelta.lib.util;

import com.arbriver.arbdelta.lib.model.Arbitrage;
import com.arbriver.arbdelta.lib.model.Match;
import com.arbriver.arbdelta.lib.model.Position;
import com.arbriver.arbdelta.lib.model.apimodel.WinWiseResponse;
import com.arbriver.arbdelta.lib.model.constants.Bookmaker;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OH {
    public static String generateMatchingKey(Map<Bookmaker, List<Position>> bookPositions, Match match) {
        StringBuilder sb = new StringBuilder();
        sb.append(match.getId()).append("-").append(match.getSport().ordinal());
        bookPositions.forEach((book, listPositions) -> {
            for(Position p : listPositions) {
                sb.append("-");
                sb.append(book.ordinal());
                String positionString = p.bet_type().toLowerCase() + p.value().toLowerCase() + p.lay();
                sb.append(Math.abs(positionString.hashCode()));
            }
        });

        return sb.toString();
    }

    public static Arbitrage arbResponseToArbitrage(WinWiseResponse winWiseResponse, Match match) {
        Arbitrage.ArbitrageBuilder arbBuilder = Arbitrage.builder();
        arbBuilder.timestamp(Instant.now());
        arbBuilder.match_id(match.getId());

        HashMap<Bookmaker, List<Position>> bookPositions = new HashMap<>();

        //if there is an arbitrage
        if(winWiseResponse.getProfit() != null &&
                !winWiseResponse.getProfit().isEmpty() &&
                winWiseResponse.getProfit().getFirst() > 0.0) {
            arbBuilder.best_profit(winWiseResponse.getProfit().getLast());
            arbBuilder.worst_profit(winWiseResponse.getProfit().getFirst());
            winWiseResponse.getBets().forEach(bet -> {
                Position p = new Position(bet.bet_type(), bet.odds(), bet.value(), bet.lay(), bet.volume(), bet.wager());
                bookPositions.computeIfAbsent(Bookmaker.valueOf(bet.bookmaker()), _ -> new ArrayList<>()).add(p);
            });
        }

        arbBuilder.matching_key(generateMatchingKey(bookPositions, match));
        arbBuilder.portfolio(bookPositions);
        return arbBuilder.build();
    }
}
