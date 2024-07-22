package com.arbriver.arbdelta.lib.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.sfn.SfnClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class AWSConfiguration {
    @Bean
    public SfnClient sfnClient() {
        return SfnClient.builder()
                .region(Region.US_EAST_1)
                .build();
    }

    @Bean
    public LambdaClient lambdaClient() {
        return LambdaClient
                .builder()
                .httpClientBuilder(ApacheHttpClient.builder()
                        .socketTimeout(Duration.of(120, TimeUnit.SECONDS.toChronoUnit())))
                .region(Region.US_EAST_1)
                .build();
    }
}
