package com.pianomastr64.usermanagement.user;

import com.pianomastr64.usermanagement.contract.HasRawPassword;
import com.pianomastr64.usermanagement.auth.RegisterRequest;
import org.mapstruct.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDTO toDTO(User user);
    
    @Mapping(target = "passwordHash", ignore = true)
    User createFromDto(UserInputDTO userDTO, @Context PasswordEncoder passwordEncoder);
    
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "role", constant = "USER")
    User createFromDto(RegisterRequest userDTO, @Context PasswordEncoder passwordEncoder);
    
    @Mapping(target = "passwordHash", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(UserInputDTO userDTO, @MappingTarget User user, @Context PasswordEncoder passwordEncoder);
    
    @AfterMapping
    default <T extends HasRawPassword> void encodePassword(
        T source, @MappingTarget User user, @Context PasswordEncoder passwordEncoder
    ) {
        if(source.password() != null) {
            user.setPasswordHash(passwordEncoder.encode(source.password()));
        }
    }
    
    default Role mapRole(String role) {
        if(role == null) {
            return null;
        }
        return Role.valueOf(role.toUpperCase());
    }
}
