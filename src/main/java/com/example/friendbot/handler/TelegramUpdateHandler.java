package com.example.friendbot.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramUpdateHandler {

    private final MessageHandler messageHandler;
    private final CallbackHandler callbackHandler;
    private final ContactHandler contactHandler;

    public void handleUpdate(Update update) {
        if (update.hasMessage()) {
            var message = update.getMessage();

            if (message.hasContact()) {
                contactHandler.handleContact(message);
            } else if (message.hasText()) {
                messageHandler.handleMessage(message);
            }

        } else if (update.hasCallbackQuery()) {
            callbackHandler.handleCallback(update.getCallbackQuery());
        }
    }
}
