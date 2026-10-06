package com.example.ms_user.controller;

import com.example.ms_user.dto.request.CreateUserRequest;
import com.example.ms_user.dto.request.LoginRequest;
import com.example.ms_user.dto.request.UpdateUserRequest;
import com.example.ms_user.dto.response.LoginResponse;
import com.example.ms_user.dto.response.UserResponse;
import com.example.ms_user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testLogin() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("john@gmail.com");
        request.setPassword("12345678");

        LoginResponse response = new LoginResponse("token");

        when(service.login(request)).thenReturn(response);

        mockMvc.perform(post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testCreateUser() throws Exception{
        CreateUserRequest request = new CreateUserRequest();
        request.setFirstName("John");
        request.setLastName("Smith");
        request.setEmail("john@gmail.com");
        request.setPassword("12345678");

        UserResponse response = new UserResponse();

        when(service.createUser(any(CreateUserRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testGetById() throws Exception {
        Long userId = 1L;

        UserResponse response = new UserResponse();

        when(service.getUserById(userId)).thenReturn(response);

        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAllUsers() throws Exception {

        UserResponse response1 = new UserResponse();
        UserResponse response2 = new UserResponse();

        when(service.getAllUsers()).thenReturn(List.of(response1, response2));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdateUser() throws Exception {
        Long userId = 1L;

        UpdateUserRequest request = new UpdateUserRequest();
        request.setFirstName("John");
        request.setLastName("Smith");
        request.setEmail("john@gmail.com");
        request.setPassword("12345678");

        UserResponse response = new UserResponse();

        when(service.updateUser(eq(userId), any(UpdateUserRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteUserById() throws Exception {
        Long userId = 1L;

        doNothing().when(service).deleteUser(userId);

        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isOk());
    }
}