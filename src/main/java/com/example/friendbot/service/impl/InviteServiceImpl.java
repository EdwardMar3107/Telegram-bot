package com.example.friendbot.service.impl;

import com.example.friendbot.model.Invite;
import com.example.friendbot.model.Invite.InviteStatus;
import com.example.friendbot.repository.InviteRepository;
import com.example.friendbot.service.InviteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InviteServiceImpl implements InviteService {

    private final InviteRepository inviteRepository;

    @Override
    @Transactional
    public Invite createInvite(Long fromChatId, Long toChatId, String placeId, String placeName, LocalDateTime dateTime) {
        Invite invite = new Invite(fromChatId, toChatId, placeId, placeName, dateTime);
        invite.setId(UUID.randomUUID().toString());
        return inviteRepository.save(invite);
    }

    @Override
    public List<Invite> getSentInvites(Long fromChatId) {
        return inviteRepository.findByFromChatIdAndStatusOrderByCreatedAtDesc(fromChatId, InviteStatus.PENDING);
    }

    @Override
    public List<Invite> getReceivedInvites(Long toChatId) {
        return inviteRepository.findByToChatId(toChatId);
    }

    @Override
    public List<Invite> getPendingReceivedInvites(Long toChatId) {
        return inviteRepository.findByToChatIdAndStatus(toChatId, InviteStatus.PENDING);
    }

    @Override
    public Optional<Invite> findInviteById(String inviteId) {
        return inviteRepository.findById(inviteId);
    }

    @Override
    @Transactional
    public Invite acceptInvite(String inviteId) {
        Invite invite = inviteRepository.findById(inviteId)
                .orElseThrow(() -> new IllegalArgumentException("Invite not found: " + inviteId));

        invite.setStatus(InviteStatus.ACCEPTED);
        return inviteRepository.save(invite);
    }

    @Override
    @Transactional
    public Invite declineInvite(String inviteId) {
        Invite invite = inviteRepository.findById(inviteId)
                .orElseThrow(() -> new IllegalArgumentException("Invite not found: " + inviteId));

        invite.setStatus(InviteStatus.DECLINED);
        return inviteRepository.save(invite);
    }

    @Override
    @Transactional
    public boolean cancelInvite(Long fromChatId, String inviteId) {
        Optional<Invite> inviteOpt = inviteRepository.findById(inviteId);

        if (inviteOpt.isPresent() && inviteOpt.get().getFromChatId().equals(fromChatId)) {
            inviteRepository.deleteById(inviteId);
            return true;
        }
        return false;
    }

    @Override
    public boolean existsPendingInviteBetween(Long fromChatId, Long toChatId) {
        return inviteRepository.existsByFromChatIdAndToChatIdAndStatus(
                fromChatId, toChatId, InviteStatus.PENDING);
    }
}
