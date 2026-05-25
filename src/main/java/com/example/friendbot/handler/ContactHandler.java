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
    private final MessageSender   sender;

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
            sender.send(chatId, "❌ Произошла ошибка при обработке контакта.");
            resetToIdle(user);
        }
    }

    private void processContact(BotUser user, Contact contact) {
        Long   ownerChatId  = user.getChatId();
        Long   friendChatId = contact.getUserId();

        //кнопка RequestContact всегда отправляет СВОЙ контакт
        //поэтому userId всегда будет совпадать с chatId пользователя
        //просто сохраняем телефон для матчинга и объясняем как добавить друга
        if (friendChatId != null && friendChatId.equals(ownerChatId)) {
            //сохраняем свой телефон - пригодится для матчинга
            if (contact.getPhoneNumber() != null) {
                String phone = contact.getPhoneNumber().replaceAll("[^0-9]", "");
                user.setPhone(phone);
                botUserService.save(user);
            }

            sender.send(ownerChatId,
                    "ℹ️ Telegram позволяет поделиться только своим контактом.\n\n" +
                            "Чтобы добавить друга — используй кнопку *➕ Добавить друга* и введи его данные вручную.\n\n" +
                            "Твой Telegram ID: `" + ownerChatId + "`\n" +
                            "Друг может узнать свой ID написав боту @userinfobot",
                    keyboardService.getMainMenuKeyboard());
            resetToIdle(user);
            return;
        }

        //этот блок на случай если вдруг придёт чужой контакт
        String firstName = contact.getFirstName() != null ? contact.getFirstName() : "";
        String lastName  = contact.getLastName()  != null ? " " + contact.getLastName() : "";
        String name      = (firstName + lastName).trim();
        if (name.isBlank()) name = "Без имени";

        String phone = contact.getPhoneNumber() != null
                ? contact.getPhoneNumber().replaceAll("[^0-9]", "")
                : null;

        try {
            friendService.addFriend(ownerChatId, name, friendChatId, phone);
        } catch (IllegalArgumentException e) {
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