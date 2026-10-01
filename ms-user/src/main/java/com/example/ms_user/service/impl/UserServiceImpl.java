package com.example.ms_user.service.impl;

import com.example.ms_user.dto.request.CreateUserRequest;
import com.example.ms_user.dto.request.UpdateUserRequest;
import com.example.ms_user.dto.response.UserResponse;
import com.example.ms_user.entity.Role;
import com.example.ms_user.entity.User;
import com.example.ms_user.exception.EmailAlreadyExistsException;
import com.example.ms_user.exception.InvalidUserDataException;
import com.example.ms_user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import com.example.ms_user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import com.example.ms_user.repository.UserRepository;
import com.example.ms_user.service.UserService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final UserMapper mapper;

    @Override
    public UserResponse createUser(CreateUserRequest request) {

        if (repository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        if (request.getPassword().length() < 8) {
            throw new InvalidUserDataException("Password must contain at least 8 characters");
        }

        User user = mapper.toEntity(request);

        if (user.getRole() == null) {
            user.setRole(Role.USER);
        }

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

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());

        User updatedUser = repository.save(user);

        return mapper.toResponse(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {

        User user = repository.findById(id)
                        .orElseThrow(() -> new UserNotFoundException("User not found"));

        repository.deleteById(id);
    }
}
