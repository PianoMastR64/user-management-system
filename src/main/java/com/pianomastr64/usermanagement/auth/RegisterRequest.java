package com.pianomastr64.usermanagement.auth;

import com.pianomastr64.usermanagement.contract.HasRawPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
    @NotBlank
    String name,
    
    @NotBlank
    @Email
    String email,
    
    @NotBlank
    String password
) implements HasRawPassword {}
