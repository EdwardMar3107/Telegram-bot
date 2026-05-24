package com.example.friendbot.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class Friend {

    private String id = UUID.randomUUID().toString().substring(0, 8);

    private String name;

    private Long friendChatId;

    private String phone;

    //список мест, связанных именно с этим другом
    private List<Place> places = new ArrayList<>();

    private LocalDateTime createdAt = LocalDateTime.now();

    public Friend(String name, Long friendChatId, String phone) {
        this.name = name;
        this.friendChatId = friendChatId;
        this.phone = phone;
        this.createdAt = LocalDateTime.now();
    }

    public void addPlace(Place place) {
        if (place != null) {
            this.places.add(place);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Friend)) return false;
        Friend other = (Friend) o;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Friend{id=" + id + ", name='" + name + "', places=" + places.size() + "}";
    }
}

