package com.pianomastr64.usermanagement.user;

import com.pianomastr64.usermanagement.security.AuthorizationSpEL;
import com.pianomastr64.usermanagement.security.CurrentUserId;
import com.pianomastr64.usermanagement.user.validation.OnCreate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/users")
@Validated
public class UserController {
    
    private final UserService service;
    
    public UserController(UserService service) {
        this.service = service;
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> createUser(@RequestBody @Validated(OnCreate.class) UserInputDTO user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createUser(user));
    }
    
    @PostMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    @Validated(OnCreate.class)
    public ResponseEntity<List<UserDTO>> createUsers(@RequestBody @NotEmpty(message = "Users list must not be empty")  List<@Valid UserInputDTO> users) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createUsers(users));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize(AuthorizationSpEL.ADMIN_OR_SELF)
    public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
        return service.getUser(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(@CurrentUserId Long id) {
        return service.getUser(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> dtos = service.getAllUsers();
        return dtos.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(dtos);
    }
    
    @PatchMapping("/{id}")
    @PreAuthorize(AuthorizationSpEL.ADMIN_OR_SELF)
    public ResponseEntity<UserDTO> updateUserPartial(@PathVariable Long id, @RequestBody @Validated UserInputDTO user) {
        return service.updateUser(id, user)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> updateUserFull(@PathVariable Long id, @RequestBody @Validated(OnCreate.class) UserInputDTO user) {
        return service.updateUser(id, user)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        return service.deleteUser(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
