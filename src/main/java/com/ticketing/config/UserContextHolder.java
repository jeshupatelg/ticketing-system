package com.ticketing.config;

import com.ticketing.model.User;

public class UserContextHolder {

    private static final ThreadLocal<User> CURRENT_USER = new ThreadLocal<>();

    public static void set(User user) {
        CURRENT_USER.set(user);
    }

    public static User get() {
        return CURRENT_USER.get();
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
