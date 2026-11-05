package com.example.ms_user.service;

import com.example.ms_user.dto.request.CreateUserRequest;
import com.example.ms_user.dto.request.LoginRequest;
import com.example.ms_user.dto.request.UpdateUserRequest;
import com.example.ms_user.dto.response.LoginResponse;
import com.example.ms_user.dto.response.UserResponse;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface UserService {

    UserResponse createUser(CreateUserRequest request);

    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal")
    UserResponse getUserById(Long id);

    @PreAuthorize("hasRole('ADMIN')")
    List<UserResponse> getAllUsers();

    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal")
    void deleteUser(Long id);

    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal")
    UserResponse updateUser(Long id, UpdateUserRequest request);

    LoginResponse login(LoginRequest request);
}
