package com.arbriver.arbdelta.lib.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sfn.SfnClient;

@Configuration
public class AWSConfiguration {
    @Bean
    public SfnClient sfnClient() {
        return SfnClient.builder()
                .region(Region.US_EAST_1)
                .build();
    }
}
