package com.example.friendbot.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class Place {

    private String id;

    private String name;

    private String address;

    private String comment;

    private String googleMapsLink;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Place(String name, String address) {
        this.name = name;
        this.address = address;
        this.createdAt = LocalDateTime.now();
    }

    public Place(String name, String address, String comment) {
        this.name = name;
        this.address = address;
        this.comment = comment;
        this.createdAt = LocalDateTime.now();
    }

    public String getDisplayName() {
        if (address == null || address.isBlank()) {
            return name;
        }
        return name + " (" + address + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Place)) return false;
        Place other = (Place) o;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Place{id=" + id + ", name='" + name + "'}";
    }
}
