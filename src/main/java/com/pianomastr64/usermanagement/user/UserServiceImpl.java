package com.pianomastr64.usermanagement.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository repo;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;
    
    public UserServiceImpl(UserRepository repo, UserMapper mapper, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    public UserDTO createUser(UserInputDTO dto) {
        User user = mapper.createFromDto(dto, passwordEncoder);
        return mapper.toDTO(repo.save(user));
    }
    
    @Override
    public List<UserDTO> createUsers(List<UserInputDTO> dtos) {
        List<User> users = dtos.stream()
            .map(dto -> mapper.createFromDto(dto, passwordEncoder))
            .toList();
        
        repo.saveAll(users);
        
        return users.stream()
            .map(mapper::toDTO)
            .toList();
    }
    
    @Override
    public Optional<UserDTO> getUser(Long id) {
        return repo.findById(id)
            .map(mapper::toDTO);
    }
    
    @Override
    public List<UserDTO> getAllUsers() {
        return repo.findAll().stream()
            .map(mapper::toDTO)
            .toList();
    }
    
    @Override
    public Optional<UserDTO> updateUser(Long id, UserInputDTO dto) {
        return repo.findById(id)
            .map(user -> {
                mapper.updateFromDto(dto, user, passwordEncoder);
                return mapper.toDTO(repo.save(user));
            });
    }
    
    /**
     * Deletes a user by ID.
     *
     * @param id the ID of the user to delete
     * @return true if the user was deleted, false if the user was not found
     */
    
    @Override
    public boolean deleteUser(Long id) {
        if(repo.existsById(id)) {
            repo.deleteById(id);
            return true;
        }
        return false;
    }
}
