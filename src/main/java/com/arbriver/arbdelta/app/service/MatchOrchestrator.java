package com.arbriver.arbdelta.app.service;

import com.arbriver.arbdelta.app.handler.MatchHandler;
import com.arbriver.arbdelta.lib.model.Fixture;
import com.arbriver.arbdelta.lib.model.Match;
import com.arbriver.arbdelta.lib.model.apimodel.WinWiseResponse;
import com.arbriver.arbdelta.lib.model.constants.Bookmaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
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
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        CompletionService<MatchHandler.ArbResponse> completionService = new ExecutorCompletionService<>(executorService);
        try {
            for (Match match : matches) {
                if(match.getStart_time().isBefore(Instant.now())) {
                    log.debug("{} started at {}. Discarding.", match.getText(), match.getStart_time());
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
                WinWiseResponse winWiseResponse = arbResponse.response();
                if(winWiseResponse == null) {
                    continue;
                }
                Match match = arbResponse.match();
                int numPositions = 0;
                for(Fixture link : match.getLinks()) numPositions += link.getPositions().size();
                log.info("Finished {}/{}: {}. {} positions processed.", i, submitTaskCount.get(), match.getText(), numPositions);
                if(winWiseResponse.getErrors() != null && !winWiseResponse.getErrors().isEmpty()) {
                    //TODO: process errors
                    //log.warn("{} had errors in the response: {}", match.getText(), winWiseResponse.getErrors());
                }

                if(winWiseResponse.getProfit() != null && !winWiseResponse.getProfit().isEmpty() && winWiseResponse.getProfit().getFirst() > 0) {
                    log.info("Arbitrage found for {}", match.getText());
                    log.info("\tProfit: {}", winWiseResponse.getProfit());
                    for(WinWiseResponse.Bet bet : winWiseResponse.getBets()) {
                        Bookmaker bookmaker = Bookmaker.valueOf(bet.bookmaker());
                        log.info("\t{}", bet);
                        for(Fixture link : match.getLinks()) {
                            if(link.getBook().equals(bookmaker)) {
                                log.info("\t{}", link.getHyperlink());
                            }
                        }
                    }
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
