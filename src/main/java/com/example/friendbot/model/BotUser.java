package com.example.friendbot.model;

import com.example.friendbot.state.UserState;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Document(collection = "bot_users")
@Getter
@Setter
@NoArgsConstructor
public class BotUser {

    @Id
    private Long chatId;

    private String username;
    private String firstName;

    @Indexed(sparse = true)
    private String phone;

    private UserState state = UserState.IDLE;

    // Embedded документы
    private List<Friend> friends = new ArrayList<>();

    // Сессионные данные (временные)
    private Map<String, String> sessionData = new HashMap<>();

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime lastActivityAt = LocalDateTime.now();

    public void putSession(String key, String value) {
        sessionData.put(key, value);
    }

    public String getSessionOrThrow(String key) {
        String value = sessionData.get(key);
        if (value == null) throw new IllegalStateException("Session key not found: " + key);
        return value;
    }

    public String getSessionOrDefault(String key, String defaultValue) {
        return sessionData.getOrDefault(key, defaultValue);
    }

    public void clearSession() {
        sessionData.clear();
    }

    public void removeSessionKey(String key) {
        sessionData.remove(key);
    }

    public Friend getFriendById(String friendId) {
        return friends.stream()
                .filter(f -> friendId.equals(f.getId()))
                .findFirst()
                .orElse(null);
    }

    public Friend getFriendByName(String name) {
        return friends.stream()
                .filter(f -> f.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public void addFriend(Friend friend) {
        if (friend != null) {
            this.friends.add(friend);
        }
    }

    public void updateLastActivity() {
        this.lastActivityAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BotUser)) return false;
        BotUser botUser = (BotUser) o;
        return Objects.equals(chatId, botUser.chatId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chatId);
    }

    @Override
    public String toString() {
        return "BotUser{chatId=" + chatId + ", username='" + username + "', friends=" + friends.size() + "}";
    }
}

