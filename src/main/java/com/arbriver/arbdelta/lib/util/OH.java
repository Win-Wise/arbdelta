package com.arbriver.arbdelta.lib.util;

import com.arbriver.arbdelta.lib.model.Arbitrage;
import com.arbriver.arbdelta.lib.model.BookPosition;
import com.arbriver.arbdelta.lib.model.Match;
import com.arbriver.arbdelta.lib.model.Position;
import com.arbriver.arbdelta.lib.model.apimodel.WinWiseResponse;
import com.arbriver.arbdelta.lib.model.constants.Bookmaker;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class OH {
    public static String generateMatchingKey(List<BookPosition> bookPositions, Match match) {
        StringBuilder sb = new StringBuilder();
        sb.append(match.getId()).append("-").append(match.getSport().ordinal());
        for (BookPosition bookPosition : bookPositions) {
            sb.append("-");
            sb.append(bookPosition.bookmaker().ordinal());
            Position p = bookPosition.position();
            String positionString = p.getBet_type().toLowerCase() + p.getValue().toLowerCase() + p.isLay();
            sb.append(Math.abs(positionString.hashCode()));
        }

        return sb.toString();
    }

    public static Arbitrage arbResponseToArbitrage(WinWiseResponse winWiseResponse, Match match) {
        Arbitrage.ArbitrageBuilder arbBuilder = Arbitrage.builder();
        arbBuilder.timestamp(Instant.now());
        arbBuilder.match_id(match.getId());

        List<BookPosition> bookPositions = new ArrayList<>();

        //if there is an arbitrage
        if(winWiseResponse.getProfit() != null &&
                !winWiseResponse.getProfit().isEmpty() &&
                winWiseResponse.getProfit().getFirst() > 0.0) {
            arbBuilder.best_profit(winWiseResponse.getProfit().getLast());
            arbBuilder.worst_profit(winWiseResponse.getProfit().getFirst());
            winWiseResponse.getBets().forEach(bet -> {
                Position p = new Position(bet.bet_type(), bet.odds(), bet.value());
                if(bet.lay()) {
                    p.setLay(true); p.setVolume(bet.volume());
                }
                bookPositions.add(new BookPosition(Bookmaker.valueOf(bet.bookmaker()), p));
            });
        }

        arbBuilder.matching_key(generateMatchingKey(bookPositions, match));
        arbBuilder.portfolio(bookPositions);
        return arbBuilder.build();
    }
}
