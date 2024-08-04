package com.arbriver.arbdelta.app.handler;

import com.arbriver.arbdelta.app.service.LambdaMatchService;
import com.arbriver.arbdelta.app.service.MongoMatchService;
import com.arbriver.arbdelta.lib.model.Arbitrage;
import com.arbriver.arbdelta.lib.model.ArbitrageBlock;
import com.arbriver.arbdelta.lib.model.Fixture;
import com.arbriver.arbdelta.lib.model.Match;
import com.arbriver.arbdelta.lib.model.apimodel.WinWiseResponse;
import com.arbriver.arbdelta.lib.model.constants.Bookmaker;
import com.arbriver.arbdelta.lib.util.OH;
import com.mongodb.client.result.UpdateResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class MatchHandler {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final LambdaMatchService matchService;
    private final WinWiseAdapter winWiseAdapter;
    private final MongoMatchService mongoMatchService;

    public MatchHandler(LambdaMatchService matchService, WinWiseAdapter winWiseAdapter, MongoMatchService mongoMatchService) {
        this.matchService = matchService;
        this.winWiseAdapter = winWiseAdapter;
        this.mongoMatchService = mongoMatchService;
    }

    public record ArbResponse(Match match, Arbitrage response) {}

    //get list of fixtures to scrape
    public List<Fixture> getValidFixtures(Match match) {
        List<Fixture> validFixtures = new ArrayList<>();
        match.getLinks().forEach((book, fixture) -> {
            //only scrape fanduel or caesars if the event is happening in next 5 min
            if(book.equals(Bookmaker.CAESARS) || book.equals(Bookmaker.FANDUEL)) {
                if(fixture.getStartTime().isBefore(Instant.now().plus(Duration.ofMinutes(5)))) {
                    validFixtures.add(fixture);
                }
            }else {
                validFixtures.add(fixture);
            }
        });
        return validFixtures;
    }

    public void populateOdds(Match match) throws InterruptedException {
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        AtomicInteger submitTaskCount = new AtomicInteger(0);
        try(ExecutorService executorService = Executors.newCachedThreadPool(threadFactory)) {
            getValidFixtures(match).forEach(fixture -> {
                executorService.submit(() -> fixture.setPositions(
                        matchService.getPositionsForFixture(fixture, match.getSport()))
                );
                submitTaskCount.incrementAndGet();
            });
            executorService.shutdown();

            if(!executorService.awaitTermination(120, TimeUnit.SECONDS)) {
                throw new InterruptedException("Timed out waiting to retrieve odds for %s after %s seconds".formatted(match.getText(), 100));
            }
        }
    }

    public ArbitrageBlock processArb(Arbitrage arb) {
        ArbitrageBlock arbBlock = mongoMatchService.getArbitrageBlock(arb.getMatching_key());
        if (arbBlock == null) {
            arbBlock = new ArbitrageBlock();
            arbBlock.setMatching_key(arb.getMatching_key());
            arbBlock.setArbitrages(List.of(arb));
            arbBlock.setFirst_seen(arb.getTimestamp());
            arbBlock.setMatch_id(arb.getMatch_id());
        } else {
            arbBlock.getArbitrages().add(arb);
        }

        arbBlock.setLast_seen(arb.getTimestamp());
        arbBlock.setLast_worst_profit(arb.getWorst_profit());
        arbBlock.setLast_best_profit(arb.getBest_profit());

        UpdateResult result = mongoMatchService.updateArbitrageBlock(arbBlock);
        if(Objects.isNull(result)) {
            log.error("Error, could not update arbitrage db for match {}", arbBlock.getMatch_id());
        }else if(result.getModifiedCount() > 0) {
            log.info("Modified {} object in arbitrages db", result.getModifiedCount());
        }else if(Objects.nonNull(result.getUpsertedId()) && !result.getUpsertedId().isNull()) {
            log.info("Inserted an object in arbitrages db");
        }

        return arbBlock;
    }

    public ArbResponse getArbResponse(Match match) throws IOException, InterruptedException {
        WinWiseResponse winWiseResponse = winWiseAdapter.getArbitrageResponse(match);
        if(winWiseResponse.getErrors() != null && !winWiseResponse.getErrors().isEmpty()) {
            //TODO: process errors
            log.warn("{} had errors in the response: {}", match.getText(), winWiseResponse.getErrors());
        }
        return new ArbResponse(match, OH.arbResponseToArbitrage(winWiseResponse, match));
    }


}
