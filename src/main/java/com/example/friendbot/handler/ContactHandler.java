package com.example.friendbot.handler;

import com.example.friendbot.bot.FriendInviteBot;
import com.example.friendbot.model.BotUser;
import com.example.friendbot.service.BotUserService;
import com.example.friendbot.service.FriendService;
import com.example.friendbot.service.KeyboardService;
import com.example.friendbot.state.UserState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContactHandler {

    private final BotUserService botUserService;
    private final FriendService friendService;
    private final KeyboardService keyboardService;
    private final FriendInviteBot bot;

    public void handleContact(Message message) {
        Long chatId = message.getChatId();
        Contact contact = message.getContact();

        if (contact == null) {
            sendMessage(chatId, "Контакт не получен.");
            return;
        }

        BotUser user = botUserService.createOrGetUser(
                chatId,
                message.getFrom().getUserName(),
                message.getFrom().getFirstName()
        );

        try {
            processContact(user, contact);
        } catch (Exception e) {
            log.error("Error processing contact from {}", chatId, e);
            sendMessage(chatId, "Произошла ошибка при обработке контакта.");
        }
    }

    private void processContact(BotUser user, Contact contact) throws TelegramApiException {
        Long ownerChatId = user.getChatId();
        String firstName = contact.getFirstName();
        String phoneNumber = contact.getPhoneNumber();
        Long friendChatId = contact.getUserId();

        if (friendChatId != null && friendChatId.equals(ownerChatId)) {
            sendMessage(ownerChatId, "🤦 Ты не можешь добавить самого себя в друзья.");
            resetToIdle(user);
            return;
        }

        // Добавляем друга
        friendService.addFriend(ownerChatId, firstName, friendChatId, phoneNumber);

        sendMessage(ownerChatId,
                "✅ Друг **" + firstName + "** успешно добавлен!",
                keyboardService.getMainMenuKeyboard());

        resetToIdle(user);
    }

    // ==================== Вспомогательные методы ====================

    private void sendMessage(Long chatId, String text) {
        sendMessage(chatId, text, null);
    }

    /**
     * Основной метод отправки сообщений
     */
    private void sendMessage(Long chatId, String text, ReplyKeyboard replyMarkup) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();

        if (replyMarkup != null) {
            sendMessage.setReplyMarkup(replyMarkup);
        }

        try {
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Failed to send message to {}: {}", chatId, e.getMessage(), e);
        }
    }

    private void resetToIdle(BotUser user) {
        user.setState(UserState.IDLE);
        user.clearSession();
        botUserService.save(user);
    }
}
