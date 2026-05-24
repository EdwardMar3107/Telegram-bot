package com.example.friendbot.config;

import com.example.friendbot.bot.FriendInviteBot;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Configuration
public class SenderConfig {

    @Bean
    public AbsSender absSender(FriendInviteBot bot) {
        return bot;
    }
}
