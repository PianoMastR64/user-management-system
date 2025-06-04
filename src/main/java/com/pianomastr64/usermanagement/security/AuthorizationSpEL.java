package com.pianomastr64.usermanagement.security;

public final class AuthorizationSpEL {
    private AuthorizationSpEL() { }
    
    public static final String ADMIN_OR_SELF = "hasRole('ADMIN') or #id == T(java.lang.Long).valueOf(principal.username)";
}
