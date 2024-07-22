package com.arbriver.arbdelta.app.handler;

import com.arbriver.arbdelta.app.service.LambdaMatchService;
import com.arbriver.arbdelta.lib.model.Fixture;
import com.arbriver.arbdelta.lib.model.Match;
import com.arbriver.arbdelta.lib.model.apimodel.WinWiseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.arns.ArnResource;

import java.io.IOException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class MatchHandler {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final LambdaMatchService matchService;
    private final WinWiseAdapter winWiseAdapter;

    public MatchHandler(LambdaMatchService matchService, WinWiseAdapter winWiseAdapter) {
        this.matchService = matchService;
        this.winWiseAdapter = winWiseAdapter;
    }

    public record ArbResponse(Match match, WinWiseResponse response) {}

    public void populateOdds(Match match) throws InterruptedException {
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        AtomicInteger submitTaskCount = new AtomicInteger(0);
        try(ExecutorService executorService = Executors.newCachedThreadPool(threadFactory)) {
            for (Fixture fixture : match.getLinks()) {
                executorService.submit(() -> fixture.setPositions(
                        matchService.getPositionsForFixture(fixture, match.getSport()))
                );
                submitTaskCount.incrementAndGet();
            }
            executorService.shutdown();

            if(!executorService.awaitTermination(120, TimeUnit.SECONDS)) {
                throw new InterruptedException("Timed out waiting to retrieve odds for %s after %s seconds".formatted(match.getText(), 100));
            }
        }
    }

    public ArbResponse getArbResponse(Match match) throws IOException, InterruptedException {
        return new ArbResponse(match, winWiseAdapter.getArbitrageResponse(match));
    }


}
