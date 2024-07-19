package com.arbriver.arbdelta.app.container;

import com.arbriver.arbdelta.app.service.MongoMatchService;
import com.arbriver.arbdelta.app.service.StateMachineService;
import com.arbriver.arbdelta.lib.model.Fixture;
import com.arbriver.arbdelta.lib.model.Match;
import com.arbriver.arbdelta.lib.model.WinWiseResponse;
import com.arbriver.arbdelta.lib.model.constants.Bookmaker;
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
        List<Match> matches = mongoMatchService.listCommonMatches();
        System.out.println(STR."Searching through \{matches.size()} matches.");
        for (Match match : matches) {
            System.out.println(STR."Looking for match \{match.text()}");
            StartSyncExecutionResponse resp = stateMachineService.sendMatchToArbProcessor(match, "arn:aws:states:us-east-1:327989636102:stateMachine:arb-adapter-statemachine");
            WinWiseResponse winWiseResponse = gson.fromJson(resp.output(), WinWiseResponse.class);
            if(winWiseResponse == null) {
                System.out.println("ERROR. RESPONSE FROM WINWISE NOT MATCHING WINWISERESPONSE SCHEMA:");
                System.out.println(resp.output());
                continue;
            }
            if(winWiseResponse.getProfit() != null && !winWiseResponse.getProfit().isEmpty() && winWiseResponse.getProfit().getFirst() > 0) {
                System.out.println(STR."Arbitrage Found: \{match.text()}");
                System.out.println(STR."Profit: \{winWiseResponse.getProfit()}");
                for(WinWiseResponse.WinWiseBet bet : winWiseResponse.getBets()) {
                    Bookmaker bookmaker = Bookmaker.valueOf(bet.bookmaker());
                    System.out.println(STR."\t\{bet}");
                    for(Fixture link : match.links()) {
                        if(link.book().equals(bookmaker)) {
                            System.out.println(STR."\t\{link.hyperlink()}");
                        }
                    }
                }
                System.out.println();
            }
        }
        System.out.println("Done.");
    }
}
