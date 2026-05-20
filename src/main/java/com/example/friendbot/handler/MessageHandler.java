package com.example.friendbot.handler;

import com.example.friendbot.bot.FriendInviteBot;
import com.example.friendbot.model.BotUser;
import com.example.friendbot.service.BotUserService;
import com.example.friendbot.service.FriendService;
import com.example.friendbot.keyboard.KeyboardService;
import com.example.friendbot.state.UserState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageHandler {

    private final BotUserService botUserService;
    private final FriendService friendService;
    private final KeyboardService keyboardService;
    private final FriendInviteBot bot;

    public void handleMessage(Message message) {
        Long chatId = message.getChatId();
        String text = message.getText();

        BotUser user = botUserService.createOrGetUser(
                chatId,
                message.getFrom().getUserName(),
                message.getFrom().getFirstName()
        );

        user.updateLastActivity();

        try {
            handleByState(user, text);
        } catch (Exception e) {
            log.error("Error handling message from {}", chatId, e);
            sendMessage(chatId, "⚠️ Произошла ошибка. Попробуйте ещё раз.");
        }
    }

    private void handleByState(BotUser user, String text) throws TelegramApiException {
        Long chatId = user.getChatId();

        if (text == null) return;

        switch (user.getState()) {
            case IDLE:
                handleIdleState(user, text);
                break;

            case ADDING_FRIEND_NAME:
                // TODO: обработка добавления друга по имени
                sendMessage(chatId, "Функция добавления друга по имени в разработке.");
                resetToIdle(user);
                break;

            case INVITE_ENTER_DATE:
                // TODO: обработка даты
                sendMessage(chatId, "Функция ввода даты в разработке.");
                resetToIdle(user);
                break;

            default:
                sendMessage(chatId, "Я пока не понимаю эту команду.");
                resetToIdle(user);
        }
    }

    private void handleIdleState(BotUser user, String text) throws TelegramApiException {
        Long chatId = user.getChatId();

        switch (text) {
            case "/start":
                sendMessage(chatId, "👋 Добро пожаловать в Friendly Invite Bot!\n\n" +
                                "Я помогаю легко договариваться о встречах с друзьями.",
                        keyboardService.getMainMenuKeyboard());
                break;

            case "👥 Мои друзья":
                var friends = friendService.getAllFriends(chatId);
                sendMessage(chatId, "Выберите друга:",
                        keyboardService.getFriendsSelectionKeyboard(friends));
                user.setState(UserState.VIEW_MY_FRIENDS);
                break;

            case "➕ Добавить друга":
                sendMessage(chatId, "Отправьте контакт или напишите имя друга.",
                        keyboardService.getCancelKeyboard());
                user.setState(UserState.ADD_FRIEND_MENU);
                break;

            case "📨 Новое приглашение":
                var allFriends = friendService.getAllFriends(chatId);
                sendMessage(chatId, "Кому хотите отправить приглашение?",
                        keyboardService.getFriendsSelectionKeyboard(allFriends));
                user.setState(UserState.INVITE_SELECT_FRIEND);
                break;

            case "📥 Мои приглашения":
                sendMessage(chatId, "Раздел «Мои приглашения» пока в разработке.");
                break;

            default:
                sendMessage(chatId, "Пожалуйста, используйте кнопки меню.",
                        keyboardService.getMainMenuKeyboard());
        }

        botUserService.save(user);
    }

    private void sendMessage(Long chatId, String text) {
        sendMessage(chatId, text, null);
    }

    private void sendMessage(Long chatId, String text, ReplyKeyboard replyMarkup) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();

        if (replyMarkup != null) {
            message.setReplyMarkup(replyMarkup);
        }

        try {
            bot.execute(message);
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