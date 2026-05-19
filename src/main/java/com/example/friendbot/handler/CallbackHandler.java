package com.example.friendbot.handler;

import com.example.friendbot.bot.FriendInviteBot;
import com.example.friendbot.model.BotUser;
import com.example.friendbot.service.BotUserService;
import com.example.friendbot.service.InviteService;
import com.example.friendbot.service.KeyboardService;
import com.example.friendbot.state.UserState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CallbackHandler {

    private final BotUserService botUserService;
    private final InviteService inviteService;
    private final KeyboardService keyboardService;
    private final FriendInviteBot bot;

    public void handleCallback(CallbackQuery callbackQuery) {
        Long chatId = callbackQuery.getMessage().getChatId();
        String data = callbackQuery.getData();
        String callbackId = callbackQuery.getId();

        BotUser user = botUserService.createOrGetUser(
                chatId,
                callbackQuery.getFrom().getUserName(),
                callbackQuery.getFrom().getFirstName()
        );

        user.updateLastActivity();

        try {
            processCallback(user, data, callbackId);
        } catch (Exception e) {
            log.error("Error processing callback {} from {}", data, chatId, e);
            answerCallback(callbackId, "❌ Произошла ошибка");
        }
    }

    private void processCallback(BotUser user, String data, String callbackId) throws TelegramApiException {
        Long chatId = user.getChatId();

        answerCallback(callbackId);

        if (data.startsWith("select_friend:")) {
            handleSelectFriend(user, data);
        } else if (data.startsWith("select_place:")) {
            handleSelectPlace(user, data);
        } else if (data.startsWith("confirm_invite:")) {
            handleConfirmInvite(user, data);
        } else if (data.startsWith("accept_invite:")) {
            handleAcceptInvite(data);
        } else if (data.startsWith("decline_invite:")) {
            handleDeclineInvite(data);
        } else if (data.equals("add_new_friend")) {
            sendMessage(chatId, "Как хочешь добавить друга?", keyboardService.getCancelKeyboard());
            user.setState(UserState.ADD_FRIEND_MENU);
            botUserService.save(user);
        } else if (data.equals("cancel_action") || data.startsWith("cancel_invite:")) {
            sendMessage(chatId, "✅ Действие отменено.", keyboardService.getMainMenuKeyboard());
            resetToIdle(user);
        } else {
            sendMessage(chatId, "Неизвестное действие.");
        }
    }

    private void handleSelectFriend(BotUser user, String data) {
        String friendId = data.split(":")[1];
        user.putSession("selectedFriendId", friendId);

        sendMessage(user.getChatId(), "Выберите место для встречи:",
                keyboardService.getPlacesSelectionKeyboard(user.getChatId(), friendId));

        user.setState(UserState.INVITE_SELECT_PLACE);
        botUserService.save(user);
    }

    private void handleSelectPlace(BotUser user, String data) {
        String[] parts = data.split(":");
        String placeId = parts[1];

        user.putSession("selectedPlaceId", placeId);

        sendMessage(user.getChatId(), "Введите дату встречи (в формате ДД.ММ.ГГГГ):");
        user.setState(UserState.INVITE_ENTER_DATE);
        botUserService.save(user);
    }

    private void handleConfirmInvite(BotUser user, String data) {
        String inviteId = data.split(":")[1];
        sendMessage(user.getChatId(), "✅ Приглашение успешно отправлено!",
                keyboardService.getMainMenuKeyboard());
        resetToIdle(user);
    }

    private void handleAcceptInvite(String data) {
        String inviteId = data.split(":")[1];
        inviteService.acceptInvite(inviteId);
        // Можно добавить уведомление инициатору
    }

    private void handleDeclineInvite(String data) {
        String inviteId = data.split(":")[1];
        inviteService.declineInvite(inviteId);
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

    private void answerCallback(String callbackId) {
        answerCallback(callbackId, null);
    }

    private void answerCallback(String callbackId, String text) {
        AnswerCallbackQuery answer = AnswerCallbackQuery.builder()
                .callbackQueryId(callbackId)
                .text(text)
                .build();

        try {
            bot.execute(answer);
        } catch (TelegramApiException e) {
            log.warn("Failed to answer callback query", e);
        }
    }

    private void resetToIdle(BotUser user) {
        user.setState(UserState.IDLE);
        user.clearSession();
        botUserService.save(user);
    }
}