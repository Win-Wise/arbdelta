package com.arbriver.arbdelta.app.handler;

import com.arbriver.arbdelta.lib.model.Match;
import com.arbriver.arbdelta.lib.model.apimodel.WinWiseRequest;
import com.arbriver.arbdelta.lib.model.apimodel.WinWiseResponse;
import com.arbriver.arbdelta.lib.model.constants.Bookmaker;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class WinWiseAdapter {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final HttpRequest.Builder baseWinWiseRequest;
    private final HttpClient httpClient;
    private final Gson gson;

    public WinWiseAdapter(@Qualifier("getarbs") HttpRequest.Builder baseWinWiseRequest, HttpClient httpClient, Gson gson) {
        //TODO initialize config. Use config to help populate request object
        this.baseWinWiseRequest = baseWinWiseRequest;
        this.httpClient = httpClient;
        this.gson = gson;
    }

    public WinWiseResponse getArbitrageResponse(Match match) throws IOException, InterruptedException {
        WinWiseRequest requestObject = matchToRequest(match);
        HttpRequest request = baseWinWiseRequest.POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestObject))).build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if(response.statusCode() >= 500) {
            throw new IOException("Internal server error from winwise getting arbitrage response");
        } else if(response.statusCode() >= 400) {
            throw new IOException("Bad request format getting arbitrage response");
        }
        return gson.fromJson(response.body(), WinWiseResponse.class);
    }

    private WinWiseRequest matchToRequest(final Match match) {
        List<WinWiseRequest.Bet> positions = new ArrayList<>();
        List<WinWiseRequest.Book> books = new ArrayList<>();

        match.getLinks().forEach((bookmaker, link) -> {
            double commission = 0.0;
            boolean ignoreWagerPrecision = false;
            if(link.getBook().equals(Bookmaker.BETFAIR)) {
                commission = 0.02;
                ignoreWagerPrecision = true;
            }
            WinWiseRequest.Book book = WinWiseRequest.Book.builder()
                    .id(link.getBook().name())
                    .commission(commission)
                    .ignore_wager_precision(ignoreWagerPrecision)
                    .build();
            books.add(book);

            link.getPositions().forEach(position -> {
                WinWiseRequest.Bet bet = WinWiseRequest.Bet.builder()
                        .value(position.value())
                        .odds(position.odds())
                        .bet_type(position.bet_type())
                        .volume(position.volume() == null ? -1.0 : position.volume())
                        .lay(position.lay())
                        .bookmaker(link.getBook().name())
                        .build();
                positions.add(bet);
            });
        });

        return WinWiseRequest.builder()
                .executionId(match.getText().toLowerCase(Locale.ROOT))
                .bets(positions)
                .bookmakers(books)
                .build();
    }
}
