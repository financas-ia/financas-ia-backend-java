package com.example.financas.user.domain.enums;

public enum RoleUser {
    ADMIN("ADMIN"),
    USER("USER");
    private String role;
    private RoleUser(String role) {
        this.role = role;
    }
    public String getRole() {
        return role;
    }
}
