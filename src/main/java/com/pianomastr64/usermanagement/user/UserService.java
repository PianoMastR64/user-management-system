package com.pianomastr64.usermanagement.user;

import java.util.List;
import java.util.Optional;

public interface UserService {
    UserDTO createUser(UserInputDTO dto);
    List<UserDTO> createUsers(List<UserInputDTO> dtos);
    Optional<UserDTO> getUser(Long id);
    List<UserDTO> getAllUsers();
    Optional<UserDTO> updateUser(Long id, UserInputDTO dto);
    boolean deleteUser(Long id);
}
