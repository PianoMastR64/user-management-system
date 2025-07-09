package com.pianomastr64.usermanagement.user;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserPersistenceTest {
    @Autowired
    private EntityManager entityManager;
    
    private User user;
    
    @BeforeEach
    void setUp() {
        user = new User("User", "user@email.com", "hashedPassword", Role.USER);
        entityManager.persist(user);
        entityManager.flush();
    }
    
    @Test
    void shouldNotPersistWithNullPasswordHash() {
        user.setPasswordHash(null);
        assertThrows(PersistenceException.class, () -> entityManager.flush());
    }
    
    @Test
    void shouldNotPersistWithBlankEmail() {
        user.setEmail(" ");
        assertThrows(PersistenceException.class, () -> entityManager.flush());
    }
}