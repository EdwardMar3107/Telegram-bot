package com.example.friendbot.state;

public enum UserState {

    // Основное состояние
    IDLE,

    //Добавление друга
    ADD_FRIEND_MENU,              // Выбор способа добавления (контакт / вручную)
    ADDING_FRIEND_NAME,           // Ожидаем имя друга (ручной ввод)
    ADDING_FRIEND_CHAT_ID,        // Ожидаем Telegram ID / username
    WAITING_FRIEND_CONTACT,       // Ожидаем отправленный контакт из Telegram

    // Добавление места
    SELECTING_FRIEND_FOR_PLACE,   // Выбор друга, к которому добавляем место
    ADDING_PLACE_NAME,            // Ввод названия места
    ADDING_PLACE_ADDRESS,         // Ввод адреса / описания места

    //Создание и отправка инвайта
    INVITE_SELECT_FRIEND,         // Выбор друга для приглашения
    INVITE_SELECT_PLACE,          // Выбор места
    INVITE_ENTER_DATE,            // Ввод даты
    INVITE_ENTER_TIME,            // Ввод времени
    INVITE_CONFIRM,               // Подтверждение перед отправкой

    //Просмотр информации
    VIEW_MY_FRIENDS,              // Просмотр списка всех друзей
    VIEW_FRIEND_PLACES,           // Просмотр мест у конкретного друга
    VIEW_MY_INVITES_SENT,         // Просмотр отправленных инвайтов
    VIEW_MY_INVITES_RECEIVED,     // Просмотр полученных инвайтов

    //Универсальные состояния
    WAITING_TEXT_INPUT,           // Универсальное состояние для любого текстового ввода
    CONFIRMATION_PENDING;          // Ожидание подтверждения какого-либо действия

    public boolean isAddingFriend() {
        return this == ADD_FRIEND_MENU ||
                this == ADDING_FRIEND_NAME ||
                this == ADDING_FRIEND_CHAT_ID ||
                this == WAITING_FRIEND_CONTACT;
    }

    public boolean isAddingPlace() {
        return this == SELECTING_FRIEND_FOR_PLACE ||
                this == ADDING_PLACE_NAME ||
                this == ADDING_PLACE_ADDRESS;
    }

    public boolean isCreatingInvite() {
        return this == INVITE_SELECT_FRIEND ||
                this == INVITE_SELECT_PLACE ||
                this == INVITE_ENTER_DATE ||
                this == INVITE_ENTER_TIME ||
                this == INVITE_CONFIRM;
    }

    public boolean isViewing() {
        return this == VIEW_MY_FRIENDS ||
                this == VIEW_FRIEND_PLACES ||
                this == VIEW_MY_INVITES_SENT ||
                this == VIEW_MY_INVITES_RECEIVED;
    }
}

