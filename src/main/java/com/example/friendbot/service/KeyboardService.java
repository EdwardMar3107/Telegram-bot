package com.example.friendbot.service;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

public interface KeyboardService {

    ReplyKeyboardMarkup getMainMenuKeyboard();

    InlineKeyboardMarkup getFriendsSelectionKeyboard(Long ownerChatId);

    InlineKeyboardMarkup getPlacesSelectionKeyboard(Long ownerChatId, String friendId);

    InlineKeyboardMarkup getInviteConfirmationKeyboard(String inviteId);

    InlineKeyboardMarkup getInviteActionKeyboard(String inviteId);

    InlineKeyboardMarkup getCancelKeyboard();

    InlineKeyboardMarkup getEmptyInlineKeyboard();
}

