package com.example.friendbot.service;

import com.example.friendbot.model.BotUser;

import java.util.Optional;

public interface BotUserService {

    BotUser createOrGetUser(Long chatId, String username, String firstName);

    Optional<BotUser> findByChatId(Long chatId);

    BotUser save(BotUser botUser);

    boolean existsByChatId(Long chatId);

    void updateLastActivity(Long chatId);

    BotUser addPhone(Long chatId, String phone);

    BotUser getByChatIdOrThrow(Long chatId);
}

