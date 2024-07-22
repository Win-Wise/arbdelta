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
                .uri(URI.create("https://multi-market-calculator.p.rapidapi.com/MultiMarket"))
                .header("Content-Type", "application/json")
                .header("x-rapidapi-key", "3e058a8afamsh7011c00ae2f5b51p1fcee3jsn281de7db5a32")
                .header("x-rapidapi-host", "multi-market-calculator.p.rapidapi.com");
    }

    @Bean
    public HttpClient getHttpClient() {
        return HttpClient.newHttpClient();
    }
}
