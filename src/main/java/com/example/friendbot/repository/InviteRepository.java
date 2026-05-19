package com.example.friendbot.repository;

import com.example.friendbot.model.Invite;
import com.example.friendbot.model.Invite.InviteStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InviteRepository extends MongoRepository<Invite, String> {

    List<Invite> findByFromChatId(Long fromChatId);
    List<Invite> findByToChatId(Long toChatId);
    List<Invite> findByToChatIdAndStatus(Long toChatId, InviteStatus status);
    List<Invite> findByFromChatIdAndStatus(Long fromChatId, InviteStatus status);

    Optional<Invite> findByFromChatIdAndToChatIdAndStatus(
            Long fromChatId, Long toChatId, InviteStatus status);

    List<Invite> findByToChatIdAndStatusOrderByCreatedAtDesc(
            Long toChatId, InviteStatus status);

    List<Invite> findByFromChatIdAndStatusOrderByCreatedAtDesc(
            Long fromChatId, InviteStatus status);

    boolean existsByFromChatIdAndToChatIdAndStatus(
            Long fromChatId, Long toChatId, InviteStatus status);
}


