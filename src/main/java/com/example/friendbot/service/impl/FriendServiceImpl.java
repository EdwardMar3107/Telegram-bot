package com.example.friendbot.service.impl;

import com.example.friendbot.model.BotUser;
import com.example.friendbot.model.Friend;
import com.example.friendbot.model.Place;
import com.example.friendbot.service.BotUserService;
import com.example.friendbot.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService {

    private final BotUserService botUserService;

    @Override
    @Transactional
    public Friend addFriend(Long ownerChatId, String name, Long friendChatId, String phone) {
        BotUser user = botUserService.getByChatIdOrThrow(ownerChatId);

        // Проверка на дубликат
        boolean exists = user.getFriends().stream()
                .anyMatch(f -> f.getName().equalsIgnoreCase(name) ||
                        (friendChatId != null && friendChatId.equals(f.getFriendChatId())));

        if (exists) {
            throw new IllegalArgumentException("Friend with name '" + name + "' already exists");
        }

        Friend friend = new Friend(name, friendChatId, phone);

        user.addFriend(friend);
        botUserService.save(user);

        return friend;
    }

    @Override
    public Friend addFriend(Long ownerChatId, String name, Long friendChatId) {
        return addFriend(ownerChatId, name, friendChatId, null);
    }

    @Override
    public Optional<Friend> findFriendById(Long ownerChatId, String friendId) {
        return botUserService.findByChatId(ownerChatId)
                .map(user -> user.getFriendById(friendId));
    }

    @Override
    public List<Friend> getAllFriends(Long ownerChatId) {
        return botUserService.findByChatId(ownerChatId)
                .map(BotUser::getFriends)
                .orElse(List.of());
    }

    @Override
    @Transactional
    public Place addPlaceToFriend(Long ownerChatId, String friendId, String placeName, String address) {
        BotUser user = botUserService.getByChatIdOrThrow(ownerChatId);

        Friend friend = user.getFriendById(friendId);
        if (friend == null) {
            throw new IllegalArgumentException("Friend not found with id: " + friendId);
        }

        Place place = new Place(placeName, address);

        friend.addPlace(place);
        botUserService.save(user);

        return place;
    }

    @Override
    public List<Place> getPlacesByFriend(Long ownerChatId, String friendId) {
        return findFriendById(ownerChatId, friendId)
                .map(Friend::getPlaces)
                .orElse(List.of());
    }

    @Override
    public boolean isFriendExists(Long ownerChatId, String name, Long friendChatId) {
        return botUserService.findByChatId(ownerChatId)
                .map(user -> user.getFriends().stream()
                        .anyMatch(f -> f.getName().equalsIgnoreCase(name) ||
                                (friendChatId != null && friendChatId.equals(f.getFriendChatId()))))
                .orElse(false);
    }
}

