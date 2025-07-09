package com.pianomastr64.usermanagement.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = UserController.class)
class UserControllerUnitTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockitoBean
    private UserService userService;
    
    @BeforeEach
    void setup() {
        given(userService.getUser(1L))
            .willReturn(Optional.of(new UserDTO(1L, "Admin", "admin@example.com", Role.ADMIN)));
        given(userService.getUser(2L))
            .willReturn(Optional.of(new UserDTO(2L, "User", "user@example.com", Role.USER)));
    }
    
    @Test
    @WithMockUser(username = "dev", roles = "ADMIN")
    void passwordHashShouldNotBeSerialized() throws Exception {
        mockMvc.perform(get("/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.passwordHash").doesNotExist());
        
        mockMvc.perform(get("/users/2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }
}