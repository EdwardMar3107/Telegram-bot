package com.example.friendbot.service;

import com.example.friendbot.model.Invite;

import java.util.List;
import java.util.Optional;

public interface InviteService {

    Invite createInvite(Long fromChatId, Long toChatId, String placeId, String placeName, String dateTime);

    List<Invite> getSentInvites(Long fromChatId);
    List<Invite> getReceivedInvites(Long toChatId);
    List<Invite> getPendingReceivedInvites(Long toChatId);

    Optional<Invite> findInviteById(String inviteId);

    Invite acceptInvite(String inviteId);
    Invite declineInvite(String inviteId);

    boolean cancelInvite(Long fromChatId, String inviteId);

    boolean existsPendingInviteBetween(Long fromChatId, Long toChatId);
}

