package com.arbriver.arbdelta.app.service;

import com.arbriver.arbdelta.lib.model.Fixture;
import com.arbriver.arbdelta.lib.model.Match;
import com.arbriver.arbdelta.lib.model.WinWiseResponse;
import com.arbriver.arbdelta.lib.model.constants.Bookmaker;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sfn.model.StartSyncExecutionResponse;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class MatchOrchestrator {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final MongoMatchService mongoMatchService;
    private final StateMachineService stateMachineService;
    private final Gson gson;


    public MatchOrchestrator(MongoMatchService mongoMatchService, StateMachineService stateMachineService, Gson gson) {
        this.mongoMatchService = mongoMatchService;
        this.stateMachineService = stateMachineService;
        this.gson = gson;
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
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        CompletionService<ArbResponse> completionService = new ExecutorCompletionService<>(executorService);
        try {
            for (Match match : matches) {
                if(match.start_time().isBefore(Instant.now())) {
                    log.debug("{} started at {}. Discarding.", match.text(), match.start_time());
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
            ArbResponse arbResponse;
            try {
                arbResponse = completionService.take().get();
                WinWiseResponse winWiseResponse = arbResponse.response();
                if(winWiseResponse == null) {
                    continue;
                }
                Match match = arbResponse.match;
                log.info("Finished {}/{}: {}", i, submitTaskCount.get(), match.text());
                if(winWiseResponse.getErrors() != null && !winWiseResponse.getErrors().isEmpty()) {
                    //TODO: process errors
                    //log.warn("{} had errors in the response: {}", match.text(), winWiseResponse.getErrors());
                }

                if(winWiseResponse.getProfit() != null && !winWiseResponse.getProfit().isEmpty() && winWiseResponse.getProfit().getFirst() > 0) {
                    log.info("Arbitrage found for {}", match.text());
                    log.info("\tProfit: {}", winWiseResponse.getProfit());
                    for(WinWiseResponse.WinWiseBet bet : winWiseResponse.getBets()) {
                        Bookmaker bookmaker = Bookmaker.valueOf(bet.bookmaker());
                        log.info("\t{}", bet);
                        for(Fixture link : match.links()) {
                            if(link.book().equals(bookmaker)) {
                                log.info("\t{}", link.hyperlink());
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

    record ArbResponse(Match match, WinWiseResponse response) {}

    class MatchProcessor implements Callable<ArbResponse> {
        private final Match match;

        public MatchProcessor(Match match) {
            this.match = match;
        }

        @Override
        public ArbResponse call() {
            StartSyncExecutionResponse resp = stateMachineService.sendMatchToArbProcessor(match,
                    "arn:aws:states:us-east-1:327989636102:stateMachine:arb-adapter-statemachine");

            if(resp.output() == null) {
                log.error("{} did not return a response from statemachine. Check execution arn: {}", match.text(), resp.executionArn());
                return new ArbResponse(match, null);
            }

            WinWiseResponse winWiseResponse = gson.fromJson(resp.output(), WinWiseResponse.class);
            if(winWiseResponse == null) {
                log.error("{} could not be processed as a winwise entity. Output: {}", match.text(), resp.output());
                return new ArbResponse(match, null);
            }

            return new ArbResponse(match, winWiseResponse);
        }
    }

}
