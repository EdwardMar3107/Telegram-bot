package com.example.friendbot.handler;

import com.example.friendbot.keyboard.KeyboardService;
import com.example.friendbot.model.BotUser;
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
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageHandler {

    private final BotUserService  botUserService;
    private final FriendService   friendService;
    private final InviteService   inviteService;
    private final KeyboardService keyboardService;
    private final MessageSender   sender;

    private static final DateTimeFormatter DATE_FORMATTER      = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter TIME_FORMATTER      = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public void handleMessage(Message message) {
        Long   chatId = message.getChatId();
        String text   = message.getText();

        BotUser user = botUserService.createOrGetUser(
                chatId,
                message.getFrom().getUserName(),
                message.getFrom().getFirstName()
        );

        if (text == null) return;

        try {
            if ("/start".equals(text)) {
                handleStart(user);
                return;
            }

            //кнопки главного меню работают из ЛЮБОГО состояния
            if (isMainMenuButton(text) && user.getState() != UserState.IDLE) {
                resetToIdle(user);
                handleIdleState(user, text);
                return;
            }

            handleByState(user, text);

        } catch (Exception e) {
            log.error("Ошибка при обработке сообщения от {}", chatId, e);
            sender.send(chatId, "⚠️ Произошла ошибка. Попробуй ещё раз.",
                    keyboardService.getMainMenuKeyboard());
            resetToIdle(user);
        }
    }

    //роутинг по состоянию

    private void handleByState(BotUser user, String text) {
        Long chatId = user.getChatId();

        switch (user.getState()) {

            case IDLE -> handleIdleState(user, text);

            case ADDING_FRIEND_NAME    -> handleAddingFriendName(user, text);
            case ADDING_FRIEND_CHAT_ID -> handleAddingFriendChatId(user, text);

            case ADDING_PLACE_NAME    -> handleAddingPlaceName(user, text);
            case ADDING_PLACE_ADDRESS -> handleAddingPlaceAddress(user, text);

            case INVITE_ENTER_DATE -> handleInviteDate(user, text);
            case INVITE_ENTER_TIME -> handleInviteTime(user, text);

            //эти состояния ждут нажатия кнопки (Callback), не текста
            case INVITE_SELECT_FRIEND,
                 INVITE_SELECT_PLACE,
                 INVITE_CONFIRM,
                 ADD_FRIEND_MENU,
                 WAITING_FRIEND_CONTACT,
                 SELECTING_FRIEND_FOR_PLACE,
                 VIEW_MY_FRIENDS,
                 VIEW_FRIEND_PLACES,
                 VIEW_MY_INVITES_SENT,
                 VIEW_MY_INVITES_RECEIVED -> sender.send(chatId,
                    "Используй кнопки для навигации или нажми /start для возврата в меню.");

            default -> {
                sender.send(chatId, "Пожалуйста, используй кнопки меню.",
                        keyboardService.getMainMenuKeyboard());
                resetToIdle(user);
            }
        }
    }

    //IDLE — обработка кнопок главного меню

    private void handleIdleState(BotUser user, String text) {
        Long chatId = user.getChatId();

        switch (text) {
            case "👥 Мои друзья" -> {
                var friends = friendService.getAllFriends(chatId);
                if (friends.isEmpty()) {
                    sender.send(chatId,
                            "У тебя пока нет друзей. Добавь первого!",
                            keyboardService.getFriendsSelectionKeyboard(friends));
                } else {
                    sender.send(chatId,
                            "Твои друзья:",
                            keyboardService.getFriendsSelectionKeyboard(friends));
                }
                user.setState(UserState.VIEW_MY_FRIENDS);
                botUserService.save(user);
            }

            case "➕ Добавить друга" -> {
                sender.send(chatId,
                        "Введи имя друга:",
                        keyboardService.getCancelKeyboard());
                user.setState(UserState.ADDING_FRIEND_NAME);
                botUserService.save(user);
            }

            case "📨 Новое приглашение" -> {
                var friends = friendService.getAllFriends(chatId);
                if (friends.isEmpty()) {
                    sender.send(chatId,
                            "Сначала добавь друга — потом сможешь его пригласить!",
                            keyboardService.getMainMenuKeyboard());
                    return;
                }
                sender.send(chatId,
                        "Кому хочешь отправить приглашение?",
                        keyboardService.getFriendsSelectionKeyboard(friends));
                user.setState(UserState.INVITE_SELECT_FRIEND);
                botUserService.save(user);
            }

            case "📥 Мои приглашения" -> {
                List<Invite> pending = inviteService.getPendingReceivedInvites(chatId);
                if (pending.isEmpty()) {
                    sender.send(chatId,
                            "У тебя нет новых приглашений.",
                            keyboardService.getMainMenuKeyboard());
                } else {
                    sender.send(chatId, "У тебя *" + pending.size() + "* новых приглашений:");
                    for (Invite invite : pending) {
                        sender.send(chatId,
                                "📍 *" + invite.getPlaceName() + "*\n" +
                                        "📅 " + invite.getDateTime().format(DATE_TIME_FORMATTER),
                                keyboardService.getInviteActionKeyboard(invite.getId()));
                    }
                }
            }

            default -> sender.send(chatId,
                    "Используй кнопки меню.",
                    keyboardService.getMainMenuKeyboard());
        }
    }

    //добавление друга

    private void handleAddingFriendName(BotUser user, String text) {
        Long chatId = user.getChatId();

        if (text.isBlank()) {
            sender.send(chatId, "Имя не может быть пустым. Введи имя друга:");
            return;
        }

        user.putSession("pendingFriendName", text.trim());
        user.setState(UserState.ADDING_FRIEND_CHAT_ID);
        botUserService.save(user);

        sender.send(chatId,
                "Введи Telegram ID друга.\n" +
                        "Он может узнать его написав боту @userinfobot\n\n" +
                        "Или введи *0* если не знаешь ID — друг подключится автоматически когда сам напишет боту /start.",
                keyboardService.getCancelKeyboard());
    }

    private void handleAddingFriendChatId(BotUser user, String text) {
        Long chatId = user.getChatId();

        Long friendChatId;
        try {
            long parsed = Long.parseLong(text.trim());
            friendChatId = parsed == 0 ? null : parsed;
        } catch (NumberFormatException e) {
            sender.send(chatId,
                    "❌ Это не похоже на ID. Должно быть число, например: 123456789\n" +
                            "Или введи *0* если не знаешь ID.\nПопробуй ещё раз:");
            return;
        }

        if (friendChatId != null && friendChatId.equals(chatId)) {
            sender.send(chatId, "🤦 Это твой собственный ID. Введи ID друга:");
            return;
        }

        String name = user.getSessionOrDefault("pendingFriendName", "Друг");

        try {
            friendService.addFriend(chatId, name, friendChatId);
        } catch (IllegalArgumentException e) {
            sender.send(chatId, "❌ " + e.getMessage(), keyboardService.getMainMenuKeyboard());
            resetToIdle(user);
            return;
        }

        String status = friendChatId != null
                ? "✅ *" + name + "* добавлен в друзья!"
                : "✅ *" + name + "* добавлен!\nКогда он напишет боту /start — вы автоматически свяжетесь.";

        sender.send(chatId, status, keyboardService.getMainMenuKeyboard());
        resetToIdle(user);
    }

    //добавление места

    private void handleAddingPlaceName(BotUser user, String text) {
        Long chatId = user.getChatId();

        if (text.isBlank()) {
            sender.send(chatId, "Название не может быть пустым. Введи название места:");
            return;
        }

        user.putSession("pendingPlaceName", text.trim());
        user.setState(UserState.ADDING_PLACE_ADDRESS);
        botUserService.save(user);

        sender.send(chatId,
                "Введи адрес места.\nИли напиши *пропустить* если адрес не нужен:",
                keyboardService.getCancelKeyboard());
    }

    private void handleAddingPlaceAddress(BotUser user, String text) {
        Long   chatId   = user.getChatId();
        String friendId = user.getSessionOrDefault("selectedFriendId", null);

        if (friendId == null) {
            sender.send(chatId, "❌ Что-то пошло не так. Начни сначала.",
                    keyboardService.getMainMenuKeyboard());
            resetToIdle(user);
            return;
        }

        String name    = user.getSessionOrDefault("pendingPlaceName", "Место");
        String address = "пропустить".equalsIgnoreCase(text.trim()) ? null : text.trim();

        Place place = friendService.addPlaceToFriend(chatId, friendId, name, address);

        sender.send(chatId,
                "✅ Место *" + place.getDisplayName() + "* добавлено!",
                keyboardService.getMainMenuKeyboard());
        resetToIdle(user);
    }

    //создание инвайта

    private void handleInviteDate(BotUser user, String text) {
        Long chatId = user.getChatId();

        try {
            DATE_FORMATTER.parse(text.trim());
        } catch (DateTimeParseException e) {
            sender.send(chatId,
                    "❌ Неверный формат даты. Введи в формате *ДД.ММ.ГГГГ*\nНапример: 25.05.2025");
            return;
        }

        user.putSession("inviteDate", text.trim());
        user.setState(UserState.INVITE_ENTER_TIME);
        botUserService.save(user);

        sender.send(chatId,
                "⏰ Введи время встречи в формате *ЧЧ:ММ*\nНапример: 18:30",
                keyboardService.getCancelKeyboard());
    }

    private void handleInviteTime(BotUser user, String text) {
        Long chatId = user.getChatId();

        try {
            TIME_FORMATTER.parse(text.trim());
        } catch (DateTimeParseException e) {
            sender.send(chatId,
                    "❌ Неверный формат времени. Введи в формате *ЧЧ:ММ*\nНапример: 18:30");
            return;
        }

        String date       = user.getSessionOrDefault("inviteDate", null);
        String friendId   = user.getSessionOrDefault("selectedFriendId", null);
        String placeId    = user.getSessionOrDefault("selectedPlaceId", null);
        String placeName  = user.getSessionOrDefault("selectedPlaceName", "Место");
        String friendName = user.getSessionOrDefault("selectedFriendName", "Друг");

        if (date == null || friendId == null || placeId == null) {
            sender.send(chatId, "❌ Данные сессии потеряны. Начни заново.",
                    keyboardService.getMainMenuKeyboard());
            resetToIdle(user);
            return;
        }

        LocalDateTime dateTime;
        try {
            dateTime = LocalDateTime.parse(date + " " + text.trim(), DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            sender.send(chatId, "❌ Ошибка при обработке даты и времени. Начни заново.",
                    keyboardService.getMainMenuKeyboard());
            resetToIdle(user);
            return;
        }

        if (dateTime.isBefore(LocalDateTime.now())) {
            sender.send(chatId, "❌ Дата встречи уже прошла. Введи другую дату:");
            user.setState(UserState.INVITE_ENTER_DATE);
            botUserService.save(user);
            return;
        }

        Optional<com.example.friendbot.model.Friend> friendOpt =
                friendService.findFriendById(chatId, friendId);

        if (friendOpt.isEmpty() || friendOpt.get().getFriendChatId() == null) {
            sender.send(chatId,
                    "❌ Друг ещё не написал боту — нельзя отправить инвайт.\n" +
                            "Попроси его написать /start боту.",
                    keyboardService.getMainMenuKeyboard());
            resetToIdle(user);
            return;
        }

        Long friendChatId = friendOpt.get().getFriendChatId();

        if (inviteService.existsPendingInviteBetween(chatId, friendChatId)) {
            sender.send(chatId,
                    "⚠️ У тебя уже есть активное приглашение для этого друга.\n" +
                            "Дождись ответа или отмени предыдущее.",
                    keyboardService.getMainMenuKeyboard());
            resetToIdle(user);
            return;
        }

        com.example.friendbot.model.Invite invite =
                inviteService.createInvite(chatId, friendChatId, placeId, placeName, dateTime);

        user.setState(UserState.INVITE_CONFIRM);
        botUserService.save(user);

        sender.send(chatId,
                "📋 *Детали встречи:*\n\n" +
                        "👤 Друг: *"  + friendName + "*\n" +
                        "📍 Место: *" + placeName  + "*\n" +
                        "📅 Дата: *"  + date       + "*\n" +
                        "⏰ Время: *" + text.trim() + "*\n\n" +
                        "Отправить приглашение?",
                keyboardService.getInviteConfirmationKeyboard(invite.getId()));
    }

    // /start

    private void handleStart(BotUser user) {
        resetToIdle(user);
        sender.send(user.getChatId(),
                "👋 Привет, *" + user.getFirstName() + "*!\n\n" +
                        "Я помогу легко договариваться о встречах с друзьями.\n" +
                        "Выбери действие:",
                keyboardService.getMainMenuKeyboard());
    }

    private boolean isMainMenuButton(String text) {
        return "👥 Мои друзья".equals(text)     ||
                "➕ Добавить друга".equals(text)  ||
                "📨 Новое приглашение".equals(text) ||
                "📥 Мои приглашения".equals(text);
    }

    private void resetToIdle(BotUser user) {
        user.setState(UserState.IDLE);
        user.clearSession();
        botUserService.save(user);
    }
}