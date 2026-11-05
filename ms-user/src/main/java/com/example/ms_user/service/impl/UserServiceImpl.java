package com.example.ms_user.service.impl;

import com.example.ms_user.dto.request.CreateUserRequest;
import com.example.ms_user.dto.request.LoginRequest;
import com.example.ms_user.dto.request.UpdateUserRequest;
import com.example.ms_user.dto.response.LoginResponse;
import com.example.ms_user.dto.response.UserResponse;
import com.example.ms_user.entity.Role;
import com.example.ms_user.entity.User;
import com.example.ms_user.exception.EmailAlreadyExistsException;
import com.example.ms_user.exception.InvalidCredentialsException;
import com.example.ms_user.exception.InvalidUserDataException;
import com.example.ms_user.exception.UserNotFoundException;
import com.example.ms_user.security.*;
import lombok.RequiredArgsConstructor;
import com.example.ms_user.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.ms_user.repository.UserRepository;
import com.example.ms_user.service.UserService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final UserMapper mapper;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse createUser(CreateUserRequest request) {

        if (repository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        if (request.getPassword().length() < 8) {
            throw new InvalidUserDataException("Password must contain at least 8 characters");
        }

        User user = mapper.toEntity(request);

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);

        User savedUser = repository.save(user);
        UserResponse response = mapper.toResponse(savedUser);

        return response;
    }

    @Override
    public UserResponse getUserById(Long id) {

        User user = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        UserResponse response = mapper.toResponse(user);

        return response;
    }

    @Override
    public List<UserResponse> getAllUsers() {

        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public UserResponse updateUser(Long id, UpdateUserRequest request) {

        User user = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!user.getEmail().equals(request.getEmail())
            && repository.existsByEmail(request.getEmail())) {

            throw new EmailAlreadyExistsException("Email already exists");
        }

        if (request.getPassword().length() < 8) {
            throw new InvalidUserDataException("Password must contain at least 8 characters");
        }

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User updatedUser = repository.save(user);

        return mapper.toResponse(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {

        User user = repository.findById(id)
                        .orElseThrow(() -> new UserNotFoundException("User not found"));

        repository.deleteById(id);
    }

    @Override
    public LoginResponse login(LoginRequest request) {

        User user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        String token = jwtService.generateToken(
                user.getId(),
                user.getRole()
        );

        return new LoginResponse(token);
    }
}
