package com.arbriver.arbdelta.app.service;

import com.arbriver.arbdelta.lib.model.Fixture;
import com.arbriver.arbdelta.lib.model.Match;
import com.arbriver.arbdelta.lib.model.Position;
import com.arbriver.arbdelta.lib.model.apimodel.WinWiseResponse;
import com.arbriver.arbdelta.lib.model.constants.Sport;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvocationType;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class LambdaMatchService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final LambdaClient lambdaClient;
    private final Gson gson;

    public LambdaMatchService(LambdaClient lambdaClient, Gson gson) {
        this.lambdaClient = lambdaClient;
        this.gson = gson;
    }

    public List<Position> getPositionsForFixture(Fixture fixture, Sport sport) {
        JsonObject requestElement = new JsonObject();
        requestElement.add("event", gson.toJsonTree(fixture));
        requestElement.addProperty("sport", sport.toString());

        InvokeRequest req = InvokeRequest.builder()
                .functionName("odds-scraper")
                .payload(
                        SdkBytes.fromByteArray(
                                requestElement.toString().getBytes(StandardCharsets.UTF_8)
                        ))
                .invocationType(InvocationType.REQUEST_RESPONSE)
                .build();

        InvokeResponse resp = lambdaClient.invoke(req);
        return gson.fromJson(resp.payload().asUtf8String(), new TypeToken<List<Position>>(){}.getType());
    }
}
