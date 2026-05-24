package com.example.friendbot.bot;

import com.example.friendbot.config.BotConfig;
import com.example.friendbot.handler.TelegramUpdateHandler;
import com.example.friendbot.sender.MessageSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
public class FriendInviteBot extends TelegramLongPollingBot {

    private final BotConfig botConfig;
    private final TelegramUpdateHandler updateHandler;

    public FriendInviteBot(BotConfig botConfig,
                           TelegramUpdateHandler updateHandler,
                           MessageSender messageSender) {
        super(botConfig.getToken());
        this.botConfig = botConfig;
        this.updateHandler = updateHandler;
        messageSender.setAbsSender(this);
    }

    @Override
    public String getBotUsername() {
        return botConfig.getUsername();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update == null) return;
        try {
            updateHandler.handleUpdate(update);
        } catch (Exception e) {
            log.error("Ошибка при обработке update: {}", e.getMessage(), e);
        }
    }
}