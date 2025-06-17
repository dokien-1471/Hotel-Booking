package com.project.hotel.controller;

import com.project.hotel.dto.UserDTO;
import com.project.hotel.security.JwtTokenProvider;
import com.project.hotel.service.UserService;
import com.project.hotel.exception.AuthenticationException;
import com.project.hotel.exception.ValidationException;
import com.project.hotel.constant.UserRole;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody Map<String, String> loginRequest) {
        try {
            if (!loginRequest.containsKey("email") || !loginRequest.containsKey("password")) {
                throw new ValidationException("Email and password are required");
            }

            String email = loginRequest.get("email");
            String password = loginRequest.get("password");

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.generateToken(authentication);

            UserDTO userDTO = userService.getUserByEmail(email);

            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("user", userDTO);

            log.info("User logged in successfully: {}", email);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            log.warn("Failed login attempt for user: {}", loginRequest.get("email"));
            throw new AuthenticationException("Invalid email or password");
        } catch (Exception e) {
            log.error("Error during login: {}", e.getMessage(), e);
            throw new AuthenticationException("Authentication failed: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserDTO userDTO) {
        try {
            if (userDTO.getPassword() == null || userDTO.getPassword().length() < 6) {
                throw new ValidationException("Password must be at least 6 characters long");
            }

            // Set default role if not provided
            if (userDTO.getRole() == null) {
                userDTO.setRole("ROLE_USER");
            }

            UserDTO createdUser = userService.createUser(userDTO);
            log.info("User registered successfully: {}", userDTO.getEmail());
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error during registration: {}", e.getMessage(), e);
            throw new ValidationException("Registration failed: " + e.getMessage());
        }
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<String> handleAuthenticationException(AuthenticationException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<String> handleValidationException(ValidationException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
}