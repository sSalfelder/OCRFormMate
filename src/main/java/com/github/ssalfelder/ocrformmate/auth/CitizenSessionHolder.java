package com.github.ssalfelder.ocrformmate.auth;

import com.github.ssalfelder.ocrformmate.model.User;

public class CitizenSessionHolder {
    private static User loggedInUser;

    public static void setUser(User user) {
        loggedInUser = user;
    }

    public static User getUser() {
        return loggedInUser;
    }

    public static boolean isLoggedIn() {
        return loggedInUser != null;
    }

    public static void clear() {
        loggedInUser = null;
    }
}
