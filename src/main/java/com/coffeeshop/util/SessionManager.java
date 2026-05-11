package com.coffeeshop.util;

import com.coffeeshop.model.User;

/**
 * Module: Util - SessionManager
 * Manages the current logged-in user session (singleton).
 */
public class SessionManager {
    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public void login(User user) { this.currentUser = user; }
    public void logout() { this.currentUser = null; }
    public User getCurrentUser() { return currentUser; }
    public boolean isLoggedIn() { return currentUser != null; }
    public boolean isAdmin() { return currentUser != null && currentUser.isAdmin(); }
}
