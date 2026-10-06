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
import com.example.ms_user.mapper.UserMapper;
import com.example.ms_user.repository.UserRepository;
import com.example.ms_user.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository repository;

    @Mock
    private UserMapper mapper;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserServiceImpl service;

    @Test
    void testCreateUserSuccessfully() {
        CreateUserRequest request = new CreateUserRequest();
        request.setFirstName("John");
        request.setLastName("Smith");
        request.setEmail("test@gmail.com");
        request.setPassword("12345678");
        request.setRole(Role.USER);

        User user = new User();
        user.setFirstName("John");
        user.setLastName("Smith");
        user.setEmail("test@gmail.com");
        user.setPassword("12345");
        user.setRole(Role.USER);

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setFirstName("John");
        savedUser.setLastName("Smith");
        savedUser.setEmail("test@gmail.com");
        savedUser.setRole(Role.USER);

        UserResponse response = new UserResponse();

        when(repository.existsByEmail("test@gmail.com")).thenReturn(false);

        when(mapper.toEntity(request)).thenReturn(user);

        when(repository.save(user)).thenReturn(savedUser);

        when(mapper.toResponse(savedUser)).thenReturn(response);

        UserResponse result = service.createUser(request);

        assertNotNull(result);
        assertEquals(response, result);

        verify(repository).existsByEmail("test@gmail.com");
        verify(mapper).toEntity(request);
        verify(repository).save(user);
        verify(mapper).toResponse(savedUser);
    }

    @Test
    void createUser_shouldSetDefaultRole_whenRoleIsNull() {
        CreateUserRequest request = new CreateUserRequest();
        request.setFirstName("John");
        request.setLastName("Smith");
        request.setEmail("test@gmail.com");
        request.setPassword("12345678");

        User user = new User();
        user.setRole(null);

        User savedUser = new User();

        UserResponse response = new UserResponse();

        when(repository.existsByEmail(request.getEmail())).thenReturn(false);

        when(mapper.toEntity(request)).thenReturn(user);

        when(repository.save(user)).thenReturn(savedUser);

        when(mapper.toResponse(savedUser)).thenReturn(response);

        service.createUser(request);

        assertEquals(Role.USER, user.getRole());

        verify(repository).save(user);
    }

    @Test
    void createUser_shouldThrowException_whenEmailAlreadyExists() {
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("test@gmail.com");
        request.setPassword("12345678");

        when(repository.existsByEmail(request.getEmail())).thenReturn(true);

        EmailAlreadyExistsException exception = assertThrows(
                EmailAlreadyExistsException.class, () -> service.createUser(request)
        );

        assertEquals("Email already exists", exception.getMessage());
    }

    @Test
    void createUser_shouldThrowInvalidUserDataException_whenPasswordIsInvalid() {
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("test@gmail.com");
        request.setPassword("1234567");

        when(repository.existsByEmail(request.getEmail())).thenReturn(false);

        InvalidUserDataException exception = assertThrows(
                InvalidUserDataException.class, () -> service.createUser(request)
        );

        assertEquals("Password must contain at least 8 characters", exception.getMessage());
    }

    @Test
    void testGetUserById() {
        Long userId = 1L;

        User user = new User();
        user.setId(userId);
        user.setEmail("test@Gmail.com");

        UserResponse response = new UserResponse();

        when(repository.findById(userId)).thenReturn(Optional.of(user));

        when(mapper.toResponse(user)).thenReturn(response);

        UserResponse result = service.getUserById(userId);

        assertNotNull(result);
        assertEquals(response, result);
    }

    @Test
    void testGetUserById_shouldThrowException_whenUserNotFound() {
        Long userId = 1L;

        when(repository.findById(userId)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> service.getUserById(userId));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testGetAllUsers() {
        User user1 = new User();
        user1.setId(1L);

        User user2 = new User();
        user2.setId(2L);

        UserResponse response1 = new UserResponse();
        UserResponse response2 = new UserResponse();

        when(repository.findAll())
                .thenReturn(List.of(user1, user2));

        when(mapper.toResponse(user1)).thenReturn(response1);

        when(mapper.toResponse(user2)).thenReturn(response2);

        List<UserResponse> result = service.getAllUsers();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(response1, result.get(0));
        assertEquals(response2, result.get(1));
    }

    @Test
    void testUpdateUser() {
        Long userId = 1L;

        UpdateUserRequest request = new UpdateUserRequest();
        request.setFirstName("John");
        request.setLastName("Smith");
        request.setEmail("john@gmail.com");
        request.setPassword("12345678");

        User user = new User();
        user.setId(userId);
        user.setFirstName("Old");
        user.setLastName("Name");
        user.setEmail("old@gmail.com");
        user.setPassword("oldpassword");

        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setFirstName("John");
        updatedUser.setLastName("Smith");
        updatedUser.setEmail("john@gmail.com");
        updatedUser.setPassword("12345678");

        UserResponse response = new UserResponse();

        when(repository.findById(userId)).thenReturn(Optional.of(user));

        when(repository.existsByEmail(request.getEmail())).thenReturn(false);

        when(repository.save(user)).thenReturn(updatedUser);

        when(mapper.toResponse(updatedUser)).thenReturn(response);

        UserResponse result = service.updateUser(userId, request);

        assertNotNull(result);
        assertEquals(response, result);

        assertEquals("John", user.getFirstName());
        assertEquals("Smith", user.getLastName());
        assertEquals("john@gmail.com", user.getEmail());
        assertEquals("12345678", user.getPassword());
    }

    @Test
    void testUpdateUser_shouldThrowException_whenEmailAlreadyExists() {
        Long userId = 1L;

        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("new@gmail.com");

        User user = new User();
        user.setId(userId);
        user.setEmail("old@gmail.com");

        when(repository.findById(userId)).thenReturn(Optional.of(user));

        when(repository.existsByEmail(request.getEmail())).thenReturn(true);

        EmailAlreadyExistsException exception = assertThrows(EmailAlreadyExistsException.class,
                () -> service.updateUser(userId, request));

        assertEquals("Email already exists", exception.getMessage());
    }

    @Test
    void testDeleteUser() {
        Long userId = 1L;

        User user = new User();
        user.setId(userId);

        when(repository.findById(userId)).thenReturn(Optional.of(user));

        service.deleteUser(userId);
    }

    @Test
    void testDeleteUser_shouldThrowException_whenUserNotFound() {
        Long userId = 1L;

        User user = new User();
        user.setId(userId);

        when(repository.findById(userId)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> service.deleteUser(userId));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testLogin() {
        LoginRequest request = new LoginRequest();
        request.setEmail("john@gmail.com");
        request.setPassword("12345678");

        User user = User.builder()
                .id(1L)
                .email("john@gmail.com")
                .password("12345678")
                .role(Role.USER)
                .build();

        String token = "test-jwt-token";

        when(repository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));

        when(jwtService.generateToken(user.getId(), user.getRole())).thenReturn(token);

        LoginResponse result = service.login(request);

        assertEquals(token, result.getToken());
    }

    @Test
    void testLogin_shouldThrowException_whenPasswordIsInvalid() {
        LoginRequest request = new LoginRequest();
        request.setEmail("john@gmail.com");
        request.setPassword("wrong password");

        User user = new User();
        user.setId(1L);
        user.setEmail("john@gmail.com");
        user.setPassword("password123");
        user.setRole(Role.USER);

        when(repository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(user));

        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> service.login(request)
        );

        assertEquals("Invalid credentials", exception.getMessage());
    }

    @Test
    void testLogin_shouldThrowException_whenUserNotFound() {
        LoginRequest request = new LoginRequest();
        request.setEmail("unknown@gmail.com");
        request.setPassword("12345678");

        when(repository.findByEmail(request.getEmail()))
                .thenReturn(Optional.empty());

        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> service.login(request)
        );

        assertEquals("Invalid credentials", exception.getMessage());
    }
}