package com.pianomastr64.usermanagement.auth;

import com.pianomastr64.usermanagement.user.UserMapper;
import com.pianomastr64.usermanagement.user.User;
import com.pianomastr64.usermanagement.user.UserRepository;
import com.pianomastr64.usermanagement.security.JwtUtil;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository repo;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    public AuthService(UserRepository repo, UserMapper mapper, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.repo = repo;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }
    
    public String authenticate(AuthRequest request) {
        User user = repo.findByEmail(request.email())
            .orElseThrow(() -> new BadCredentialsException("Invalid email"));
        
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid password");
        }
        
        return jwtUtil.generateToken(user.getId());
    }
    
    public String register(RegisterRequest request) {
        if (repo.existsByEmail(request.email())) {
            throw new DuplicateKeyException("Email already in use");
        }
        
        User user = mapper.createFromDto(request, passwordEncoder);
        repo.save(user);

        return jwtUtil.generateToken(user.getId());
    }
}
