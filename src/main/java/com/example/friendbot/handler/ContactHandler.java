package com.example.friendbot.handler;

import com.example.friendbot.keyboard.KeyboardService;
import com.example.friendbot.model.BotUser;
import com.example.friendbot.sender.MessageSender;
import com.example.friendbot.service.BotUserService;
import com.example.friendbot.service.FriendService;
import com.example.friendbot.state.UserState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.Message;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContactHandler {

    private final BotUserService  botUserService;
    private final FriendService   friendService;
    private final KeyboardService keyboardService;
    private final MessageSender sender;

    public void handleContact(Message message) {
        Long    chatId  = message.getChatId();
        Contact contact = message.getContact();

        if (contact == null) {
            sender.send(chatId, "Контакт не получен. Попробуй ещё раз.");
            return;
        }

        BotUser user = botUserService.createOrGetUser(
                chatId,
                message.getFrom().getUserName(),
                message.getFrom().getFirstName()
        );

        try {
            processContact(user, contact);
        } catch (Exception e) {
            log.error("Ошибка при обработке контакта от {}", chatId, e);
            sender.send(chatId, "❌ Произошла ошибка при добавлении контакта.");
            resetToIdle(user);
        }
    }

    private void processContact(BotUser user, Contact contact) {
        Long   ownerChatId  = user.getChatId();
        Long   friendChatId = contact.getUserId();
        String firstName    = contact.getFirstName() != null ? contact.getFirstName() : "";
        String lastName     = contact.getLastName()  != null ? " " + contact.getLastName() : "";
        String name         = (firstName + lastName).trim();
        String phone        = contact.getPhoneNumber() != null
                ? contact.getPhoneNumber().replaceAll("[^0-9]", "") // нормализуем телефон
                : null;

        if (name.isBlank()) name = "Без имени";

        //защита от добавления самого себя
        if (friendChatId != null && friendChatId.equals(ownerChatId)) {
            sender.send(ownerChatId,
                    "🤦 Ты не можешь добавить самого себя в друзья.",
                    keyboardService.getMainMenuKeyboard());
            resetToIdle(user);
            return;
        }

        try {
            friendService.addFriend(ownerChatId, name, friendChatId, phone);
        } catch (IllegalArgumentException e) {
            //дубликат - друг уже добавлен
            sender.send(ownerChatId,
                    "⚠️ *" + name + "* уже есть в твоём списке друзей.",
                    keyboardService.getMainMenuKeyboard());
            resetToIdle(user);
            return;
        }

        String status = friendChatId != null
                ? "✅ Уже использует бота — можешь отправлять приглашения!"
                : "⏳ Пока не писал боту. Как только напишет /start — автоматически подключится.";

        sender.send(ownerChatId,
                "✅ *" + name + "* добавлен в друзья!\n" + status,
                keyboardService.getMainMenuKeyboard());

        resetToIdle(user);
    }

    private void resetToIdle(BotUser user) {
        user.setState(UserState.IDLE);
        user.clearSession();
        botUserService.save(user);
    }
}