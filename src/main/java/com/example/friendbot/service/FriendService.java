package com.example.friendbot.service;

import com.example.friendbot.model.Friend;
import com.example.friendbot.model.Place;

import java.util.List;
import java.util.Optional;

public interface FriendService {

    Friend addFriend(Long ownerChatId, String name, Long friendChatId, String phone);

    Friend addFriend(Long ownerChatId, String name, Long friendChatId);

    Optional<Friend> findFriendById(Long ownerChatId, String friendId);

    List<Friend> getAllFriends(Long ownerChatId);

    Place addPlaceToFriend(Long ownerChatId, String friendId, String placeName, String address);

    List<Place> getPlacesByFriend(Long ownerChatId, String friendId);

    boolean isFriendExists(Long ownerChatId, String name, Long friendChatId);
}

