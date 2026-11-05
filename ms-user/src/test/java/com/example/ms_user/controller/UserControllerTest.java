package com.example.ms_user.controller;

import com.example.ms_user.dto.request.CreateUserRequest;
import com.example.ms_user.dto.request.LoginRequest;
import com.example.ms_user.dto.request.UpdateUserRequest;
import com.example.ms_user.dto.response.LoginResponse;
import com.example.ms_user.dto.response.UserResponse;
import com.example.ms_user.security.JwtService;
import com.example.ms_user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.example.ms_user.util.TestConstants.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService service;

    @MockitoBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testLogin() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail(EMAIL);
        request.setPassword(PASSWORD);

        LoginResponse response = new LoginResponse("token");

        when(service.login(request)).thenReturn(response);

        mockMvc.perform(post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testCreateUser() throws Exception{

        CreateUserRequest request = CreateUserRequest.builder()
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .email(EMAIL)
                .password(PASSWORD)
                .build();

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
        UserResponse response = new UserResponse();

        when(service.getUserById(USER_ID)).thenReturn(response);

        mockMvc.perform(get("/users/{id}", USER_ID))
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
        UpdateUserRequest request = UpdateUserRequest.builder()
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .email(EMAIL)
                .password(PASSWORD)
                .build();

        UserResponse response = new UserResponse();

        when(service.updateUser(eq(USER_ID), any(UpdateUserRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/users/{id}", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteUserById() throws Exception {
        doNothing().when(service).deleteUser(USER_ID);

        mockMvc.perform(delete("/users/{id}", USER_ID))
                .andExpect(status().isOk());
    }
}