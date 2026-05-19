package com.example.friendbot.state;

public enum UserState {

    // Основное состояние
    IDLE,

    //Добавление друга
    ADD_FRIEND_MENU,
    ADDING_FRIEND_NAME,
    ADDING_FRIEND_CHAT_ID,
    WAITING_FRIEND_CONTACT,

    // Добавление места
    SELECTING_FRIEND_FOR_PLACE,
    ADDING_PLACE_NAME,
    ADDING_PLACE_ADDRESS,

    //Создание и отправка инвайта
    INVITE_SELECT_FRIEND,
    INVITE_SELECT_PLACE,
    INVITE_ENTER_DATE,
    INVITE_ENTER_TIME,
    INVITE_CONFIRM,

    //Просмотр информации
    VIEW_MY_FRIENDS,
    VIEW_FRIEND_PLACES,
    VIEW_MY_INVITES_SENT,
    VIEW_MY_INVITES_RECEIVED,

    //Универсальные состояния
    WAITING_TEXT_INPUT,
    CONFIRMATION_PENDING;

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

    public boolean isIdle() {
        return this == IDLE;
    }
}

