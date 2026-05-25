package com.example.friendbot.keyboard.impl;

import com.example.friendbot.keyboard.KeyboardService;
import com.example.friendbot.model.Friend;
import com.example.friendbot.model.Place;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Service
public class KeyboardServiceImpl implements KeyboardService {

    //главное менюю

    @Override
    public ReplyKeyboardMarkup getMainMenuKeyboard() {
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("👥 Мои друзья"));
        row1.add(new KeyboardButton("➕ Добавить друга"));

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("📨 Новое приглашение"));
        row2.add(new KeyboardButton("📥 Мои приглашения"));

        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setKeyboard(List.of(row1, row2));
        markup.setResizeKeyboard(true);
        markup.setOneTimeKeyboard(false);
        return markup;
    }

    //список друзей

    @Override
    public InlineKeyboardMarkup getFriendsSelectionKeyboard(List<Friend> friends) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (Friend friend : friends) {
            rows.add(List.of(
                    InlineKeyboardButton.builder()
                            .text(friend.getName())
                            .callbackData("select_friend:" + friend.getId())
                            .build()
            ));
        }

        rows.add(List.of(
                InlineKeyboardButton.builder()
                        .text(friends.isEmpty() ? "➕ Добавить первого друга" : "➕ Добавить друга")
                        .callbackData("add_new_friend")
                        .build()
        ));

        return buildInline(rows);
    }

    //список мест

    @Override
    public InlineKeyboardMarkup getPlacesSelectionKeyboard(List<Place> places, String friendId) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (Place place : places) {
            rows.add(List.of(
                    InlineKeyboardButton.builder()
                            .text(place.getDisplayName())
                            .callbackData("select_place:" + place.getId() + ":" + friendId)
                            .build()
            ));
        }

        if (places.isEmpty()) {
            rows.add(List.of(
                    InlineKeyboardButton.builder()
                            .text("➕ Добавить место для этого друга")
                            .callbackData("add_place:" + friendId)
                            .build()
            ));
        }

        //кнопка "Назад" добавляется ДО setKeyboard
        rows.add(List.of(
                InlineKeyboardButton.builder()
                        .text("◀️ Назад к друзьям")
                        .callbackData("back_to_friends")
                        .build()
        ));

        return buildInline(rows);
    }

    //подтверждение инвайта

    @Override
    public InlineKeyboardMarkup getInviteConfirmationKeyboard(String inviteId) {
        return buildInline(List.of(List.of(
                InlineKeyboardButton.builder()
                        .text("✅ Отправить приглашение")
                        .callbackData("confirm_invite:" + inviteId)
                        .build(),
                InlineKeyboardButton.builder()
                        .text("❌ Отмена")
                        .callbackData("cancel_invite:" + inviteId)
                        .build()
        )));
    }

    //ответ на инвайт

    @Override
    public InlineKeyboardMarkup getInviteActionKeyboard(String inviteId) {
        return buildInline(List.of(List.of(
                InlineKeyboardButton.builder()
                        .text("✅ Принять")
                        .callbackData("accept_invite:" + inviteId)
                        .build(),
                InlineKeyboardButton.builder()
                        .text("❌ Отклонить")
                        .callbackData("decline_invite:" + inviteId)
                        .build()
        )));
    }

    //кнопка отмены

    @Override
    public InlineKeyboardMarkup getCancelKeyboard() {
        return buildInline(List.of(List.of(
                InlineKeyboardButton.builder()
                        .text("❌ Отмена")
                        .callbackData("cancel_action")
                        .build()
        )));
    }

    //запрос контакта - оставлен для совместимости с интерфейсом

    @Override
    public ReplyKeyboardMarkup getRequestContactKeyboard() {
        //Telegram позволяет делиться только своим контактом через эту кнопку.
        //для добавления друга используется ручной ввод.
        KeyboardButton btn = new KeyboardButton("📱 Поделиться своим номером");
        btn.setRequestContact(true);

        KeyboardRow row = new KeyboardRow();
        row.add(btn);

        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setKeyboard(List.of(row));
        markup.setResizeKeyboard(true);
        markup.setOneTimeKeyboard(true);
        return markup;
    }

    private InlineKeyboardMarkup buildInline(List<List<InlineKeyboardButton>> rows) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }
}