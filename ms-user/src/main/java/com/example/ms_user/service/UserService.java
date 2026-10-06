package com.example.ms_user.service;

import com.example.ms_user.dto.request.CreateUserRequest;
import com.example.ms_user.dto.request.LoginRequest;
import com.example.ms_user.dto.request.UpdateUserRequest;
import com.example.ms_user.dto.response.LoginResponse;
import com.example.ms_user.dto.response.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse createUser(CreateUserRequest request);

    UserResponse getUserById(Long id);

    List<UserResponse> getAllUsers();

    void deleteUser(Long id);

    UserResponse updateUser(Long id, UpdateUserRequest request);

    LoginResponse login(LoginRequest request);
}
