package com.coffeeshop.model;

/**
 * Module: Model - User
 * Represents kasir/admin user of the system.
 */
public class User {
    private int id;
    private String username;
    private String password;
    private String role; // "admin" or "kasir"
    private String nama;

    public User() {}

    public User(int id, String username, String role, String nama) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.nama = nama;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }
    public boolean isAdmin() { return "admin".equals(role); }

    @Override
    public String toString() { return nama + " (" + role + ")"; }
}
