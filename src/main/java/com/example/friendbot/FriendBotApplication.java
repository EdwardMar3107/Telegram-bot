package com.example.friendbot;

import com.example.friendbot.bot.FriendInviteBot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
public class FriendBotApplication {

    static void main(String[] args) throws TelegramApiException {
        ApplicationContext context = SpringApplication.run(FriendBotApplication.class, args);

        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(context.getBean(FriendInviteBot.class));
    }
}