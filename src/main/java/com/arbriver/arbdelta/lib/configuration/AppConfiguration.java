package com.arbriver.arbdelta.lib.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfiguration {
    @Bean
    public Gson getGson() {
        return new GsonBuilder().create();
    }
}
