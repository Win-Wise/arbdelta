package com.arbriver.arbdelta.app.container;

import com.arbriver.arbdelta.app.service.MongoMatchService;
import com.arbriver.arbdelta.app.service.StateMachineService;
import com.arbriver.arbdelta.lib.model.Match;
import com.arbriver.arbdelta.lib.model.WinWiseResponse;
import com.google.gson.Gson;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sfn.model.StartSyncExecutionResponse;

import java.util.List;

@Component
public class DefaultAppContainer {
    private final StateMachineService stateMachineService;
    private final MongoMatchService mongoMatchService;
    private final Gson gson;

    public DefaultAppContainer(StateMachineService stateMachineService, MongoMatchService mongoMatchService, Gson gson) {
        this.stateMachineService = stateMachineService;
        this.mongoMatchService = mongoMatchService;
        this.gson = gson;
    }

    public void start() {
        List<Match> matches = mongoMatchService.listMatches();
        for (Match match : matches) {
            StartSyncExecutionResponse resp = stateMachineService.sendMatchToArbProcessor(match, "arn:aws:states:us-east-1:327989636102:stateMachine:arb-adapter-statemachine");
            WinWiseResponse winWiseResponse = gson.fromJson(resp.output(), WinWiseResponse.class);
            if(!winWiseResponse.getProfit().isEmpty() && winWiseResponse.getProfit().getFirst() > 0) {
                System.out.println(STR."Arbitrage Found: \{match.text()}");
                System.out.println(STR."Profit: \{winWiseResponse.getProfit()}");
                for(WinWiseResponse.WinWiseBet bet : winWiseResponse.getBets()) {
                    System.out.println(STR."\t\{bet}");
                }
                System.out.println();
            }
        }
    }
}
