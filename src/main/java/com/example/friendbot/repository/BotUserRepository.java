package com.example.friendbot.repository;

import com.example.friendbot.model.BotUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BotUserRepository extends MongoRepository<BotUser, Long> {
    Optional<BotUser> findByPhone(String phone);
}

