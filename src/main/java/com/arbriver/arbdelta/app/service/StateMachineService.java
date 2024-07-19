package com.arbriver.arbdelta.app.service;

import com.arbriver.arbdelta.lib.model.Match;
import com.arbriver.arbdelta.lib.model.converters.GsonInstantAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sfn.SfnClient;
import software.amazon.awssdk.services.sfn.model.*;

import java.time.Instant;
import java.util.List;

@Service
public class StateMachineService {
    private final SfnClient sfnClient;

    public StateMachineService(SfnClient sfnClient) {
        this.sfnClient = sfnClient;
    }

    public void listMachines() {
        try {
            ListStateMachinesResponse response = sfnClient.listStateMachines();
            List<StateMachineListItem> machines = response.stateMachines();
            for (StateMachineListItem machine : machines) {
                System.out.println("The name of the state machine is: " + machine.name());
                System.out.println("The ARN value is : " + machine.stateMachineArn());
            }

        } catch (SfnException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }

    public StartSyncExecutionResponse sendMatchToArbProcessor(Match match, String stateMachineArn) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter( Instant.class , new GsonInstantAdapter() )
                .create();
        StartSyncExecutionRequest request = StartSyncExecutionRequest.builder()
                .stateMachineArn(stateMachineArn)
                .name(match.text().replaceAll("[^A-Za-z0-9]", "").replace(" ","-"))
                .input(gson.toJson(match))
                .build();


        return sfnClient.startSyncExecution(request);
    }
}
