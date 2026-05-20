package com.example.friendbot.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Objects;

@Document(collection = "invites")
@Getter
@Setter
@NoArgsConstructor
public class Invite {

    @Id
    private String id;

    private Long fromChatId;
    private Long toChatId;

    private String placeId;
    private String placeName;

    private LocalDateTime dateTime;

    private InviteStatus status = InviteStatus.PENDING;

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum InviteStatus {
        PENDING,
        ACCEPTED,
        DECLINED
    }

    public Invite(Long fromChatId, Long toChatId, String placeId, String placeName, LocalDateTime dateTime) {
        this.fromChatId = fromChatId;
        this.toChatId = toChatId;
        this.placeId = placeId;
        this.placeName = placeName;
        this.dateTime = dateTime;
        this.status = InviteStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Invite)) return false;
        Invite other = (Invite) o;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Invite{id=" + id + ", from=" + fromChatId + ", to=" + toChatId +
                ", dateTime=" + dateTime + ", status=" + status + "}";
    }
}
