package com.example.friendbot.bot;

import com.example.friendbot.config.BotConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

@Slf4j
@Component
@RequiredArgsConstructor
public class FriendInviteBot extends TelegramLongPollingBot {

    private final TelegramUpdateHandler updateHandler;
    private final BotConfig botConfig;

    @Override
    public String getBotUsername() {
        return botConfig.getUsername();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update == null) {
            return;
        }

        try {
            updateHandler.handleUpdate(update);
        } catch (Exception e) {
            log.error("Error processing update: {}", e.getMessage(), e);
        }
    }
}
