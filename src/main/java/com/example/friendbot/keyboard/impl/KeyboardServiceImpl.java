package com.example.friendbot.keyboard.impl;

import com.example.friendbot.keyboard.KeyboardService;
import com.example.friendbot.model.Friend;
import com.example.friendbot.model.Place;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KeyboardServiceImpl implements KeyboardService {

    @Override
    public ReplyKeyboardMarkup getMainMenuKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("👥 Мои друзья"));
        row1.add(new KeyboardButton("➕ Добавить друга"));

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("📨 Новое приглашение"));
        row2.add(new KeyboardButton("📥 Мои приглашения"));

        keyboard.add(row1);
        keyboard.add(row2);

        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }

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

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }

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
                            .text("➕ Добавить место")
                            .callbackData("add_place:" + friendId)
                            .build()
            ));
        }

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }

    @Override
    public InlineKeyboardMarkup getInviteConfirmationKeyboard(String inviteId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(List.of(
                List.of(
                        InlineKeyboardButton.builder().text("✅ Отправить приглашение").callbackData("confirm_invite:" + inviteId).build(),
                        InlineKeyboardButton.builder().text("❌ Отмена").callbackData("cancel_invite:" + inviteId).build()
                )
        ));
        return markup;
    }

    @Override
    public InlineKeyboardMarkup getInviteActionKeyboard(String inviteId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(List.of(
                List.of(
                        InlineKeyboardButton.builder().text("✅ Принять").callbackData("accept_invite:" + inviteId).build(),
                        InlineKeyboardButton.builder().text("❌ Отклонить").callbackData("decline_invite:" + inviteId).build()
                )
        ));
        return markup;
    }

    @Override
    public InlineKeyboardMarkup getCancelKeyboard() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(List.of(
                List.of(InlineKeyboardButton.builder()
                        .text("❌ Отмена")
                        .callbackData("cancel_action")
                        .build())
        ));
        return markup;
    }
}