package com.class_management.entity;
public enum Role {
    ADMIN,
    USER;
    public String getAuthority() {
        return "ROLE_" + name();
    }
}