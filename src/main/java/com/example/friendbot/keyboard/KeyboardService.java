package com.example.friendbot.keyboard;

import com.example.friendbot.model.Friend;
import com.example.friendbot.model.Place;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.util.List;

public interface KeyboardService {

    ReplyKeyboardMarkup getMainMenuKeyboard();

    InlineKeyboardMarkup getFriendsSelectionKeyboard(List<Friend> friends);

    InlineKeyboardMarkup getPlacesSelectionKeyboard(List<Place> places, String friendId);

    InlineKeyboardMarkup getInviteConfirmationKeyboard(String inviteId);

    InlineKeyboardMarkup getInviteActionKeyboard(String inviteId);

    InlineKeyboardMarkup getCancelKeyboard();

    ReplyKeyboardMarkup getRequestContactKeyboard();

}