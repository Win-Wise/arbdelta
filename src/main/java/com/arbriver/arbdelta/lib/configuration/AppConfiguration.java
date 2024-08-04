package com.arbriver.arbdelta.lib.configuration;

import com.arbriver.arbdelta.lib.model.converters.GsonInstantAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Instant;

@Configuration
public class AppConfiguration {
    @Bean
    public Gson getGson() {
        return new GsonBuilder()
                .registerTypeAdapter(Instant.class, new GsonInstantAdapter())
                .create();
    }

    @Bean("getarbs")
    public HttpRequest.Builder getBaseWinwiseRequest() {
        //TODO make this not hardcoded
        return HttpRequest.newBuilder()
                .uri(URI.create("https://zp1nlkf7qk.execute-api.eu-west-2.amazonaws.com/MultiMarket"))
                .header("Content-Type", "application/json")
                .header("x-rapidapi-proxy-secret", "a3d4f1b2-e7c6-b5d8-9123-0f4b67e8a9c1");
    }

    @Bean
    public HttpClient getHttpClient() {
        return HttpClient.newHttpClient();
    }
}
