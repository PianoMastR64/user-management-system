package com.pianomastr64.usermanagement.user;

import com.pianomastr64.usermanagement.exception.DuplicateEmailException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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
        checkUniqueEmail(dto, null);
        
        User user = mapper.createFromDto(dto, passwordEncoder);
        return mapper.toDTO(repo.save(user));
    }
    
    @Override
    public List<UserDTO> createUsers(List<UserInputDTO> dtos) {
        checkUniqueEmailBulk(dtos);
        
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
        checkUniqueEmail(dto, id);
        
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
    
    private void checkUniqueEmailBulk(List<UserInputDTO> dtos) {
        String throwMessage = "";
        
        //check against self first
        Set<String> duplicateEmailsInDto = dtos.stream()
            .collect(Collectors.groupingBy(UserInputDTO::email, Collectors.counting()))
            .entrySet().stream()
            .filter(entry -> entry.getValue() > 1)
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
        if(!duplicateEmailsInDto.isEmpty()) {
            throwMessage = "Duplicate email(s) found in payload: " + duplicateEmailsInDto;
        }
        
        //check against existing users
        List<String> duplicateEmailsInDb = repo.findExistingEmails(
            dtos.stream()
                .map(UserInputDTO::email)
                .toList()
        );
        if(!duplicateEmailsInDb.isEmpty()) {
            if(!throwMessage.isEmpty()) {
                throwMessage += ", ";
            }
            throwMessage += "Email(s) already in use: " + duplicateEmailsInDb;
        }
        
        if(!throwMessage.isEmpty()) {
            throw new DuplicateEmailException(throwMessage);
        }
    }
    
    private void checkUniqueEmail(UserInputDTO dto, Long excludeId) {
        boolean emailTaken = (excludeId == null)
            ? repo.existsByEmail(dto.email())
            : repo.existsByEmailAndIdNot(dto.email(), excludeId);
        
        if(emailTaken) {
            throw new DuplicateEmailException("Email is already in use: " + dto.email());
        }
    }
}