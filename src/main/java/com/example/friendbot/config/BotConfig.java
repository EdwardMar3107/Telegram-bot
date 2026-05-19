package com.example.friendbot.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(prefix = "bot")
@Getter
@Setter
@Validated
public class BotConfig {
    private String token;
    private String username;
}

