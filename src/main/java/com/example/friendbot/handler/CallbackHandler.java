package com.example.friendbot.handler;

import com.example.friendbot.keyboard.KeyboardService;
import com.example.friendbot.model.BotUser;
import com.example.friendbot.model.Friend;
import com.example.friendbot.model.Invite;
import com.example.friendbot.model.Place;
import com.example.friendbot.sender.MessageSender;
import com.example.friendbot.service.BotUserService;
import com.example.friendbot.service.FriendService;
import com.example.friendbot.service.InviteService;
import com.example.friendbot.state.UserState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CallbackHandler {

    private final BotUserService  botUserService;
    private final FriendService   friendService;
    private final InviteService   inviteService;
    private final KeyboardService keyboardService;
    private final MessageSender sender;

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public void handleCallback(CallbackQuery callbackQuery) {
        Long   chatId     = callbackQuery.getMessage().getChatId();
        String data       = callbackQuery.getData();
        String callbackId = callbackQuery.getId();

        BotUser user = botUserService.createOrGetUser(
                chatId,
                callbackQuery.getFrom().getUserName(),
                callbackQuery.getFrom().getFirstName()
        );

        //убираем часики на кнопке, всегда, даже если будет ошибка
        sender.answerCallback(callbackId);

        try {
            processCallback(user, data);
        } catch (Exception e) {
            log.error("Ошибка при обработке callback {} от {}", data, chatId, e);
            sender.send(chatId, "❌ Произошла ошибка. Попробуй ещё раз.");
            resetToIdle(user);
        }
    }

    private void processCallback(BotUser user, String data) {
        Long chatId = user.getChatId();

        //выбор друга для инвайта
        if (data.startsWith("select_friend:")) {
            handleSelectFriend(user, data);

            //выбор места
        } else if (data.startsWith("select_place:")) {
            handleSelectPlace(user, data);

            //подтверждение отправки инвайта
        } else if (data.startsWith("confirm_invite:")) {
            handleConfirmInvite(user, data);

            //отмена конкретного инвайта
        } else if (data.startsWith("cancel_invite:")) {
            handleCancelInvite(user, data);

            //принятие инвайта
        } else if (data.startsWith("accept_invite:")) {
            handleAcceptInvite(user, data);

            //отклонение инвайта
        } else if (data.startsWith("decline_invite:")) {
            handleDeclineInvite(user, data);

            //добавить нового друга
        } else if (data.equals("add_new_friend")) {
            sender.send(chatId,
                    "Как хочешь добавить друга?",
                    keyboardService.getCancelKeyboard());
            user.setState(UserState.ADD_FRIEND_MENU);
            botUserService.save(user);

            //добавить место для друга
        } else if (data.startsWith("add_place:")) {
            String friendId = data.split(":")[1];
            user.putSession("selectedFriendId", friendId);
            user.setState(UserState.ADDING_PLACE_NAME);
            botUserService.save(user);
            sender.send(chatId,
                    "Введи название места (например: Кофейня Даблби):",
                    keyboardService.getCancelKeyboard());

            //назад к списку друзей
        } else if (data.equals("back_to_friends")) {
            handleBackToFriends(user);

            //отмена любого действия
        } else if (data.equals("cancel_action")) {
            sender.send(chatId, "✅ Действие отменено.", keyboardService.getMainMenuKeyboard());
            resetToIdle(user);

        } else {
            log.warn("Неизвестный callback: {} от {}", data, chatId);
            sender.send(chatId, "Неизвестное действие.", keyboardService.getMainMenuKeyboard());
        }
    }

    private void handleSelectFriend(BotUser user, String data) {
        String friendId = data.split(":")[1];
        Long   chatId   = user.getChatId();

        Optional<Friend> friendOpt = friendService.findFriendById(chatId, friendId);
        if (friendOpt.isEmpty()) {
            sender.send(chatId, "❌ Друг не найден. Попробуй ещё раз.",
                    keyboardService.getMainMenuKeyboard());
            resetToIdle(user);
            return;
        }

        Friend friend = friendOpt.get();

        //проверяем регистрацию друга в боте
        if (friend.getFriendChatId() == null) {
            sender.send(chatId,
                    "⏳ *" + friend.getName() + "* ещё не написал боту.\n" +
                            "Попроси его написать /start — после этого ты сможешь отправлять ему инвайты.",
                    keyboardService.getMainMenuKeyboard());
            resetToIdle(user);
            return;
        }

        user.putSession("selectedFriendId", friendId);
        user.putSession("selectedFriendChatId", friend.getFriendChatId().toString());
        user.putSession("selectedFriendName", friend.getName());

        List<Place> places = friendService.getPlacesByFriend(chatId, friendId);

        sender.send(chatId,
                "Выбери место для встречи с *" + friend.getName() + "*:",
                keyboardService.getPlacesSelectionKeyboard(places, friendId));

        user.setState(UserState.INVITE_SELECT_PLACE);
        botUserService.save(user);
    }

    private void handleSelectPlace(BotUser user, String data) {
        String[] parts = data.split(":");
        if (parts.length < 3) {
            sender.send(user.getChatId(), "❌ Ошибка при выборе места.");
            resetToIdle(user);
            return;
        }

        String placeId  = parts[1];
        String friendId = parts[2];

        //находим место внутри списка друга
        Optional<Friend> friendOpt = friendService.findFriendById(user.getChatId(), friendId);
        if (friendOpt.isEmpty()) {
            sender.send(user.getChatId(), "❌ Друг не найден.");
            resetToIdle(user);
            return;
        }

        Place place = friendOpt.get().getPlaces().stream()
                .filter(p -> placeId.equals(p.getId()))
                .findFirst()
                .orElse(null);

        if (place == null) {
            sender.send(user.getChatId(), "❌ Место не найдено.");
            resetToIdle(user);
            return;
        }

        user.putSession("selectedPlaceId",   placeId);
        user.putSession("selectedPlaceName", place.getDisplayName());

        sender.send(user.getChatId(),
                "📅 Введи дату встречи в формате *ДД.ММ.ГГГГ*\nНапример: 25.05.2025",
                keyboardService.getCancelKeyboard());

        user.setState(UserState.INVITE_ENTER_DATE);
        botUserService.save(user);
    }

    private void handleConfirmInvite(BotUser user, String data) {
        String inviteId = data.split(":")[1];
        Long   chatId   = user.getChatId();

        //достаём инвайт из БД, он был создан в MessageHandler после ввода времени
        Optional<Invite> inviteOpt = inviteService.findInviteById(inviteId);
        if (inviteOpt.isEmpty()) {
            sender.send(chatId, "❌ Инвайт не найден. Попробуй создать заново.",
                    keyboardService.getMainMenuKeyboard());
            resetToIdle(user);
            return;
        }

        Invite invite = inviteOpt.get();

        //проверяем что этот инвайт принадлежит этому пользователю
        if (!chatId.equals(invite.getFromChatId())) {
            sender.send(chatId, "❌ Это не твой инвайт.");
            resetToIdle(user);
            return;
        }

        //отправляем уведомление другу
        sender.send(invite.getToChatId(),
                "🎉 *" + user.getFirstName() + "* приглашает тебя на встречу!\n\n" +
                        "📍 *Место:* " + invite.getPlaceName() + "\n" +
                        "📅 *Когда:* " + invite.getDateTime().format(DATE_TIME_FORMATTER) + "\n\n" +
                        "Пойдёшь?",
                keyboardService.getInviteActionKeyboard(invite.getId()));

        //подтверждаем отправителю
        sender.send(chatId,
                "✅ Приглашение отправлено *" + user.getSessionOrDefault("selectedFriendName", "другу") + "*!",
                keyboardService.getMainMenuKeyboard());

        resetToIdle(user);
    }

    private void handleCancelInvite(BotUser user, String data) {
        String inviteId = data.split(":")[1];
        Long   chatId   = user.getChatId();

        inviteService.cancelInvite(chatId, inviteId);
        sender.send(chatId, "✅ Приглашение отменено.", keyboardService.getMainMenuKeyboard());
        resetToIdle(user);
    }

    private void handleAcceptInvite(BotUser user, String data) {
        String inviteId = data.split(":")[1];
        Long   chatId   = user.getChatId();

        Optional<Invite> inviteOpt = inviteService.findInviteById(inviteId);
        if (inviteOpt.isEmpty()) {
            sender.send(chatId, "❌ Приглашение не найдено или уже недействительно.");
            return;
        }

        Invite invite = inviteService.acceptInvite(inviteId);

        //уведомляем получателя
        sender.send(chatId, "✅ Встреча подтверждена!\n\n" +
                        "📍 " + invite.getPlaceName() + "\n" +
                        "📅 " + invite.getDateTime().format(DATE_TIME_FORMATTER),
                keyboardService.getMainMenuKeyboard());

        //уведомляем отправителя
        sender.send(invite.getFromChatId(),
                "🎉 Твоё приглашение принято!\n\n" +
                        "📍 " + invite.getPlaceName() + "\n" +
                        "📅 " + invite.getDateTime().format(DATE_TIME_FORMATTER),
                keyboardService.getMainMenuKeyboard());
    }

    private void handleDeclineInvite(BotUser user, String data) {
        String inviteId = data.split(":")[1];
        Long   chatId   = user.getChatId();

        Optional<Invite> inviteOpt = inviteService.findInviteById(inviteId);
        if (inviteOpt.isEmpty()) {
            sender.send(chatId, "❌ Приглашение не найдено.");
            return;
        }

        Invite invite = inviteService.declineInvite(inviteId);

        //уведомляем получателя
        sender.send(chatId, "Отклонено. В другой раз! 🙂",
                keyboardService.getMainMenuKeyboard());

        //уведомляем отправителя
        sender.send(invite.getFromChatId(),
                "😔 Твоё приглашение отклонено.\n" +
                        "Попробуй предложить другое время или место!",
                keyboardService.getMainMenuKeyboard());
    }

    private void handleBackToFriends(BotUser user) {
        Long         chatId  = user.getChatId();
        List<Friend> friends = friendService.getAllFriends(chatId);

        sender.send(chatId,
                "Выбери друга:",
                keyboardService.getFriendsSelectionKeyboard(friends));

        user.setState(UserState.INVITE_SELECT_FRIEND);
        botUserService.save(user);
    }

    private void resetToIdle(BotUser user) {
        //загружаем свежий объект из БД чтобы не затереть изменения
        BotUser freshUser = botUserService.getByChatIdOrThrow(user.getChatId());
        freshUser.setState(UserState.IDLE);
        freshUser.clearSession();
        botUserService.save(freshUser);
    }
}