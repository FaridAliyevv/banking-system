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
import com.example.ms_user.security.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static com.example.ms_user.util.TestConstants.*;
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

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl service;

    @Test
    void testCreateUser() {
        CreateUserRequest request = CreateUserRequest.builder()
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .email(EMAIL)
                .password(PASSWORD)
                .build();

        User user = User.builder()
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .email(EMAIL)
                .password(PASSWORD)
                .role(Role.USER)
                .build();

        User savedUser = User.builder()
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .email(EMAIL)
                .password(PASSWORD)
                .role(Role.USER)
                .build();

        UserResponse response = new UserResponse();

        when(repository.existsByEmail(EMAIL)).thenReturn(false);

        when(mapper.toEntity(request)).thenReturn(user);

        when(repository.save(user)).thenReturn(savedUser);

        when(mapper.toResponse(savedUser)).thenReturn(response);

        UserResponse result = service.createUser(request);

        assertNotNull(result);
        assertEquals(response, result);
    }

    @Test
    void createUser_shouldSetDefaultRole_whenRoleIsNull() {
        CreateUserRequest request = CreateUserRequest.builder()
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .email(EMAIL)
                .password(PASSWORD)
                .build();

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
    }

    @Test
    void createUser_shouldThrowException_whenEmailAlreadyExists() {
        CreateUserRequest request = CreateUserRequest.builder()
                .email(EMAIL)
                .password(PASSWORD)
                .build();

        when(repository.existsByEmail(request.getEmail())).thenReturn(true);

        EmailAlreadyExistsException exception = assertThrows(
                EmailAlreadyExistsException.class, () -> service.createUser(request)
        );

        assertEquals("Email already exists", exception.getMessage());
    }

    @Test
    void createUser_shouldThrowInvalidUserDataException_whenPasswordIsInvalid() {
        CreateUserRequest request = CreateUserRequest.builder()
                .email(EMAIL)
                .password(INVALID_PASSWORD)
                .build();

        when(repository.existsByEmail(request.getEmail())).thenReturn(false);

        InvalidUserDataException exception = assertThrows(
                InvalidUserDataException.class, () -> service.createUser(request)
        );

        assertEquals("Password must contain at least 8 characters", exception.getMessage());
    }

    @Test
    void testGetUserById() {
        User user = User.builder()
                .id(USER_ID)
                .email(EMAIL)
                .build();

        UserResponse response = new UserResponse();

        when(repository.findById(USER_ID)).thenReturn(Optional.of(user));

        when(mapper.toResponse(user)).thenReturn(response);

        UserResponse result = service.getUserById(USER_ID);

        assertNotNull(result);
        assertEquals(response, result);
    }

    @Test
    void testGetUserById_shouldThrowException_whenUserNotFound() {
        when(repository.findById(USER_ID)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> service.getUserById(USER_ID));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testGetAllUsers() {
        User user1 = new User();
        user1.setId(USER_ID);

        User user2 = new User();
        user2.setId(USER_ID2);

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
        UpdateUserRequest request = UpdateUserRequest.builder()
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .email(EMAIL)
                .password(PASSWORD)
                .build();

        User user = User.builder()
                .id(USER_ID)
                .firstName(OLD_FIRST_NAME)
                .lastName(OLD_LAST_NAME)
                .email(OLD_EMAIL)
                .password(OLD_PASSWORD)
                .build();

        User updatedUser = User.builder()
                .id(USER_ID)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .email(EMAIL)
                .password(PASSWORD)
                .build();

        UserResponse response = new UserResponse();

        when(repository.findById(USER_ID)).thenReturn(Optional.of(user));

        when(repository.existsByEmail(request.getEmail())).thenReturn(false);

        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);

        when(repository.save(user)).thenReturn(updatedUser);

        when(mapper.toResponse(updatedUser)).thenReturn(response);

        UserResponse result = service.updateUser(USER_ID, request);

        assertNotNull(result);
        assertEquals(response, result);

        assertEquals(FIRST_NAME, user.getFirstName());
        assertEquals(LAST_NAME, user.getLastName());
        assertEquals(EMAIL, user.getEmail());
        assertEquals(ENCODED_PASSWORD, user.getPassword());
    }

    @Test
    void testUpdateUser_shouldThrowException_whenEmailAlreadyExists() {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .email(EMAIL)
                .build();

        User user = new User();
        user.setId(USER_ID);
        user.setEmail(OLD_EMAIL);

        when(repository.findById(USER_ID)).thenReturn(Optional.of(user));

        when(repository.existsByEmail(request.getEmail())).thenReturn(true);

        EmailAlreadyExistsException exception = assertThrows(EmailAlreadyExistsException.class,
                () -> service.updateUser(USER_ID, request));

        assertEquals("Email already exists", exception.getMessage());
    }

    @Test
    void testUpdateUser_shouldNotCheckEmail_whenEmailIsNotChanged() {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .email(EMAIL)
                .password(PASSWORD)
                .build();

        User user = new User();
        user.setId(USER_ID);
        user.setEmail(EMAIL);

        User updatedUser = new User();
        UserResponse response = new UserResponse();

        when(repository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(repository.save(user)).thenReturn(updatedUser);
        when(mapper.toResponse(updatedUser)).thenReturn(response);

        UserResponse result = service.updateUser(USER_ID, request);

        assertEquals(response, result);
        verify(repository, never()).existsByEmail(any());
    }

    @Test
    void testUpdateUser_shouldThrowException_whenPasswordIsLessThan8Characters() {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .email(EMAIL)
                .password(INVALID_PASSWORD)
                .build();

        User user = User.builder()
                .firstName(OLD_FIRST_NAME)
                .lastName(OLD_LAST_NAME)
                .email(OLD_EMAIL)
                .password(OLD_PASSWORD)
                .build();

        when(repository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(repository.existsByEmail(request.getEmail())).thenReturn(false);

        InvalidUserDataException exception = assertThrows(InvalidUserDataException.class,
                () -> service.updateUser(USER_ID, request));

        assertEquals("Password must contain at least 8 characters", exception.getMessage());

    }

    @Test
    void testDeleteUser() {
        User user = new User();
        user.setId(USER_ID);

        when(repository.findById(USER_ID)).thenReturn(Optional.of(user));

        service.deleteUser(USER_ID);
    }

    @Test
    void testDeleteUser_shouldThrowException_whenUserNotFound() {
        User user = new User();
        user.setId(USER_ID);

        when(repository.findById(USER_ID)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> service.deleteUser(USER_ID));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testLogin() {
        LoginRequest request = new LoginRequest();
        request.setEmail(EMAIL);
        request.setPassword(PASSWORD);

        User user = User.builder()
                .id(USER_ID)
                .email(EMAIL)
                .password(PASSWORD)
                .role(Role.USER)
                .build();

        when(repository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));

        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);

        when(jwtService.generateToken(user.getId(), user.getRole())).thenReturn(TOKEN);

        LoginResponse result = service.login(request);

        assertEquals(TOKEN, result.getToken());
    }

    @Test
    void testLogin_shouldThrowException_whenPasswordIsInvalid() {
        LoginRequest request = new LoginRequest();
        request.setEmail(EMAIL);
        request.setPassword(INVALID_PASSWORD);

        User user = new User();
        user.setId(USER_ID);
        user.setEmail(EMAIL);
        user.setPassword(PASSWORD);
        user.setRole(Role.USER);

        when(repository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(false);

        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> service.login(request)
        );

        assertEquals("Invalid credentials", exception.getMessage());
    }

    @Test
    void testLogin_shouldThrowException_whenUserNotFound() {
        LoginRequest request = new LoginRequest();
        request.setEmail(UNKNOWN_EMAIL);
        request.setPassword(PASSWORD);

        when(repository.findByEmail(request.getEmail()))
                .thenReturn(Optional.empty());

        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> service.login(request)
        );

        assertEquals("Invalid credentials", exception.getMessage());
    }
}