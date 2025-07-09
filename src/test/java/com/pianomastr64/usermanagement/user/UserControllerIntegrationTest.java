package com.pianomastr64.usermanagement.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pianomastr64.usermanagement.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerIntegrationTest {
    
    @Autowired MockMvc mockMvc;
    @Autowired UserRepository repo;
    @Autowired PasswordEncoder encoder;
    @Autowired JwtUtil jwtUtil;
    @Autowired ObjectMapper mapper;
    
    private String adminToken;
    private String userToken;
    
    private static final UserInputDTO NEW_USER_INPUT =
        new UserInputDTO("NewUser", "new@user.com", "pass1234", "USER");
    
    @BeforeEach
    void setUp() throws Exception {
        repo.deleteAll();
        adminToken = saveAndLogin("TestAdmin", "admin@test.com", "aPwd", Role.ADMIN);
        userToken = saveAndLogin("TestUser", "user@test.com", "uPwd", Role.USER);
    }
    
    
    String saveAndLogin(String name, String email, String password, Role role) throws Exception {
        repo.save(new User(name, email, encoder.encode(password), role));
        
        String jsonRequest = mapper.writeValueAsString(Map.of(
            "email", email,
            "password", password
        ));
        
        MvcResult result = mockMvc.perform(post("/auth/login")
                .contentType(APPLICATION_JSON)
                .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
        
        String response = result.getResponse().getContentAsString();
        return mapper.readTree(response).get("token").asText();
    }
    
    private RequestPostProcessor bearer(String token) {
        return req -> {
            req.addHeader("Authorization", "Bearer " + token);
            return req;
        };
    }
    
    private Long id(String token) {
        return jwtUtil.extractId(token);
    }
    
    @Test
    void adminCanCreateUser() throws Exception {
        mockMvc.perform(post("/users")
                .with(bearer(adminToken))
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(NEW_USER_INPUT)))
            .andExpect(status().isCreated());
    }
    
    @Test
    void userCannotCreateUser() throws Exception {
        mockMvc.perform(post("/users")
                .with(bearer(userToken))
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(NEW_USER_INPUT)))
            .andExpect(status().isForbidden());
    }
    
    @Test
    void adminCanGetUserById() throws Exception {
        mockMvc.perform(get("/users/" + id(userToken))
                .with(bearer(adminToken))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk());
    }
    
    @Test
    void userCanGetOwnDetails() throws Exception {
        mockMvc.perform(get("/users/me")
                .with(bearer(userToken))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk());
    }
    
    @Test
    void adminCanDeleteUser() throws Exception {
        Long userId = id(userToken);
        
        mockMvc.perform(delete("/users/" + userId)
                .with(bearer(adminToken)))
            .andExpect(status().isNoContent());
        
        // Verify user is deleted
        mockMvc.perform(get("/users/" + userId)
                .with(bearer(adminToken)))
            .andExpect(status().isNotFound());
    }
    
    @Test
    void userCannotDeleteAnotherUser() throws Exception {
        String victimToken = saveAndLogin("Victim", "victim@user.com", "pwd", Role.USER);
        
        mockMvc.perform(delete("/users/" + id(victimToken))
                .with(bearer(userToken)))
            .andExpect(status().isForbidden());
    }
    
    @Test
    void adminCanBulkCreateUsers() throws Exception {
        UserInputDTO secondUser = new UserInputDTO(
            "SecondUser", "second@User.com", "pass1234", "USER");
        
        var users = List.of(
            NEW_USER_INPUT,
            secondUser
        );
        
        mockMvc.perform(post("/users/bulk")
                .with(bearer(adminToken))
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(users)))
            .andExpect(status().isCreated());
    }
}