package com.github.ssalfelder.ocrformmate.auth;

import com.github.ssalfelder.ocrformmate.model.Clerk;

public class ClerkSessionHolder {
    private static Clerk loggedInClerk;

    public static void setLoggedInClerk(Clerk clerk) {
        ClerkSessionHolder.loggedInClerk = clerk;
    }

    public static Clerk getLoggedInClerk() {
        return loggedInClerk;
    }

    public static void clear() {
        loggedInClerk = null;
    }
}
