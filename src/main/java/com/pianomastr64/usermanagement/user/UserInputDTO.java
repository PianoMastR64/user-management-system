package com.pianomastr64.usermanagement.user;

import com.pianomastr64.usermanagement.contract.HasRawPassword;
import com.pianomastr64.usermanagement.user.validation.NotBlankIfPresent;
import com.pianomastr64.usermanagement.user.validation.OnCreate;
import com.pianomastr64.usermanagement.user.validation.ValidEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserInputDTO(
    @NotNull(groups = OnCreate.class)
    @NotBlankIfPresent
    String name,
    
    @NotNull(groups = OnCreate.class)
    @NotBlankIfPresent
    @Email
    //TODO: consider custom validator for unique email
    String email,
    
    @NotNull(groups = OnCreate.class)
    @NotBlankIfPresent
    @Size(min = 8, message = "Password must be at least 8 characters long")
    //TODO: write custom validator for password strength
    String password,
    
    @NotNull(groups = OnCreate.class)
    @NotBlankIfPresent
    @ValidEnum(enumClass = Role.class)
    String role
) implements HasRawPassword {}