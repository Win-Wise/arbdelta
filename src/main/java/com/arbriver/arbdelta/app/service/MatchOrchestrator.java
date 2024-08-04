package com.arbriver.arbdelta.app.service;

import com.arbriver.arbdelta.app.handler.MatchHandler;
import com.arbriver.arbdelta.lib.model.Arbitrage;
import com.arbriver.arbdelta.lib.model.BookPosition;
import com.arbriver.arbdelta.lib.model.Fixture;
import com.arbriver.arbdelta.lib.model.Match;
import com.arbriver.arbdelta.lib.model.constants.Bookmaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class MatchOrchestrator {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final MongoMatchService mongoMatchService;
    private final MatchHandler matchHandler;


    public MatchOrchestrator(MongoMatchService mongoMatchService, MatchHandler matchHandler) {
        this.mongoMatchService = mongoMatchService;
        this.matchHandler = matchHandler;
    }

    public void orchestrate() {
        while(true) {
            cycleOnce();
        }
    }

    public void cycleOnce() {
        List<Match> matches = mongoMatchService.listCommonMatches();
        log.info("Starting cycle. Processing {} matches", matches.size());
        AtomicInteger submitTaskCount = new AtomicInteger(0);
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        CompletionService<MatchHandler.ArbResponse> completionService = new ExecutorCompletionService<>(executorService);
        try {
            for (Match match : matches) {
                if(match.getStartTime().isBefore(Instant.now())) {
                    log.debug("{} started at {}. Discarding.", match.getText(), match.getStartTime());
                    continue;
                }
                completionService.submit(new MatchProcessor(match));
                submitTaskCount.incrementAndGet();
            }
            log.info("{} valid matches submitted", submitTaskCount.get());
        } finally {
            executorService.shutdown();
        }


        for (int i = 0; i < Math.min(submitTaskCount.get(), matches.size()); i++) {
            MatchHandler.ArbResponse arbResponse;
            try {
                arbResponse = completionService.take().get();
                Arbitrage arbitrage = arbResponse.response();
                if(arbitrage == null) {
                    continue;
                }
                Match match = arbResponse.match();
                int numPositions = 0;
                for(Map.Entry<Bookmaker, Fixture> link : match.getLinks().entrySet()) numPositions += link.getValue().getPositions().size();
                log.info("Finished {}/{}: {}. {} positions processed.", i, submitTaskCount.get(), match.getText(), numPositions);

                if(arbitrage.getBest_profit() > 0.0) {
                    log.info("Arbitrage found for {}. Made up of {} bets", match.getText(), arbitrage.getPortfolio().size());
                    log.info("\tBest Profit: {}, Worst Profit: {}", arbitrage.getBest_profit(), arbitrage.getWorst_profit());
                    for(BookPosition bet : arbitrage.getPortfolio()) {
                        Bookmaker bookmaker = Bookmaker.valueOf(bet.bookmaker().name());
                        log.info("\t{}", bet);
                        Fixture link = match.getLinks().get(bookmaker);
//                        log.info("\t{}", link.getHyperlink());
                    }
                    matchHandler.processArb(arbitrage);
                }
            } catch (ExecutionException | InterruptedException ex) {
                log.error("Received error during computation for result in Thread [{}] {}", Thread.currentThread().threadId(), ex.getMessage());
            }
        }
        log.info("Completed Cycle. Processed {} matches.", submitTaskCount.get());
    }

    class MatchProcessor implements Callable<MatchHandler.ArbResponse> {
        private final Match match;

        public MatchProcessor(Match match) {
            this.match = match;
        }

        @Override
        public MatchHandler.ArbResponse call() {
            try {
                matchHandler.populateOdds(match);
                return matchHandler.getArbResponse(match);

            } catch(Exception ex) {
                log.error("Error processing match {}", match.getText(), ex);
                return new MatchHandler.ArbResponse(match, null);
            }
        }
    }

}
