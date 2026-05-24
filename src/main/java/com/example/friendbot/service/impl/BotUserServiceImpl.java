package com.example.friendbot.service.impl;

import com.example.friendbot.model.BotUser;
import com.example.friendbot.repository.BotUserRepository;
import com.example.friendbot.service.BotUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BotUserServiceImpl implements BotUserService {

    private final BotUserRepository botUserRepository;

    @Override
    public BotUser createOrGetUser(Long chatId, String username, String firstName) {
        return botUserRepository.findById(chatId)
                .orElseGet(() -> {
                    BotUser newUser = new BotUser();
                    newUser.setChatId(chatId);
                    newUser.setUsername(username);
                    newUser.setFirstName(firstName);
                    return botUserRepository.save(newUser);
                });
    }

    @Override
    public Optional<BotUser> findByChatId(Long chatId) {
        return botUserRepository.findById(chatId);
    }

    @Override
    public BotUser save(BotUser botUser) {
        if (botUser == null) {
            throw new IllegalArgumentException("BotUser cannot be null");
        }
        botUser.updateLastActivity();
        return botUserRepository.save(botUser);
    }

    @Override
    public boolean existsByChatId(Long chatId) {
        return botUserRepository.existsById(chatId);
    }

    @Override
    @Transactional
    public void updateLastActivity(Long chatId) {
        botUserRepository.findById(chatId).ifPresent(user -> {
            user.updateLastActivity();
            botUserRepository.save(user);
        });
    }

    @Override
    public BotUser addPhone(Long chatId, String phone) {
        BotUser user = getByChatIdOrThrow(chatId);
        user.setPhone(phone);
        return save(user);
    }

    @Override
    public BotUser getByChatIdOrThrow(Long chatId) {
        return findByChatId(chatId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with chatId: " + chatId));
    }
}

