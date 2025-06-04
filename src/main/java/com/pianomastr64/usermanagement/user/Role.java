package com.pianomastr64.usermanagement.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

//TODO consider moving to "role" package if role code gets more complex
public enum Role {
    USER,
    ADMIN;
    
    @JsonCreator
    public static Role fromString(String role) {
        return valueOf(role.toUpperCase());
    }
    
    @JsonValue
    public String toJson() {
        return name().toLowerCase();
    }
}
