package com.example.ms_account.service.impl;

import com.example.ms_account.client.UserClient;
import com.example.ms_account.dto.request.CreateAccountRequest;
import com.example.ms_account.dto.request.UpdateAccountRequest;
import com.example.ms_account.dto.response.AccountResponse;
import com.example.ms_account.dto.response.UserResponse;
import com.example.ms_account.entity.Account;
import com.example.ms_account.enums.AccountStatus;
import com.example.ms_account.enums.AccountType;
import com.example.ms_account.enums.UserRole;
import com.example.ms_account.exception.*;
import com.example.ms_account.mapper.AccountMapper;
import com.example.ms_account.repository.AccountRepository;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.example.ms_account.util.TestConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository repository;

    @Mock
    private AccountMapper mapper;

    @Mock
    private UserClient client;

    @InjectMocks
    private AccountServiceImpl service;

    @Test
    void testCreateAccount() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setUserId(USER_ID);
        request.setAccountType(AccountType.DEBIT);

        Account account = new Account();
        Account savedAccount = new Account();
        AccountResponse response = new AccountResponse();

        UserResponse userResponse = UserResponse.builder()
                .id(USER_ID)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .email(EMAIL)
                .password(PASSWORD)
                .role(UserRole.USER)
                .build();

        when(client.getUserById(USER_ID, TOKEN)).thenReturn(userResponse);

        when(mapper.toEntity(request)).thenReturn(account);

        when(repository.save(account)).thenReturn(savedAccount);

        when(mapper.toResponse(savedAccount)).thenReturn(response);

        AccountResponse result = service.createAccount(request, TOKEN);

        assertEquals(response, result);
    }

    @Test
    void testCreateAccount_shouldThrowException_whenUserNotFound() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setUserId(USER_ID);

        FeignException.NotFound feignException = mock(FeignException.NotFound.class);

        when(client.getUserById(USER_ID, TOKEN)).thenThrow(feignException);

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> service.createAccount(request, TOKEN));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testCreateAccount_shouldThrowAccessDenied_whenUserClientReturnsForbidden() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setUserId(USER_ID);
        request.setAccountType(AccountType.DEBIT);

        FeignException.Forbidden forbidden = mock(FeignException.Forbidden.class);

        when(client.getUserById(USER_ID, TOKEN)).thenThrow(forbidden);

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> service.createAccount(request, TOKEN));
    }

    @Test
    void testGetAccountById() {
        Account account = new Account();
        account.setId(ACCOUNT_ID);

        AccountResponse response = new AccountResponse();

        when(repository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));

        when(mapper.toResponse(account)).thenReturn(response);

        AccountResponse result = service.getAccountById(ACCOUNT_ID);

        assertNotNull(result);
        assertEquals(response, result);
    }

    @Test
    void testGetAccountById_shouldThrowException_whenAccountNotFound() {
        when(repository.findById(ACCOUNT_ID)).thenReturn(Optional.empty());

        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class,
                () -> service.getAccountById(ACCOUNT_ID));

        assertEquals("Account not found", exception.getMessage());
    }

    @Test
    void testGetAccountByIban() {
        Account account = new Account();
        account.setIban(IBAN);

        AccountResponse response = new AccountResponse();

        when(repository.findByIban(IBAN)).thenReturn(Optional.of(account));

        when(mapper.toResponse(account)).thenReturn(response);

        AccountResponse result = service.getAccountByIban(IBAN);

        assertNotNull(result);
        assertEquals(response, result);
    }

    @Test
    void testGetAccountByIban_shouldThrowException_whenAccountNotFound() {
        when(repository.findByIban(IBAN)).thenReturn(Optional.empty());

        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class,
                () -> service.getAccountByIban(IBAN));

        assertEquals("Account not found", exception.getMessage());
    }

    @Test
    void testGetAllAccountsByUserId() {
        Account account1 = new Account();
        Account account2 = new Account();

        AccountResponse response1 = new AccountResponse();
        AccountResponse response2 = new AccountResponse();

        when(repository.findByUserId(USER_ID)).thenReturn(List.of(account1, account2));

        when(mapper.toResponse(account1)).thenReturn(response1);

        when(mapper.toResponse(account2)).thenReturn(response2);

        List<AccountResponse> result = service.getAllAccountsByUserId(USER_ID);

        assertEquals(2, result.size());
        assertEquals(response1, result.get(0));
        assertEquals(response2, result.get(1));

    }

    @Test
    void testGetAllAccounts() {
        Account account1 = new Account();
        Account account2 = new Account();

        AccountResponse response1 = new AccountResponse();
        AccountResponse response2 = new AccountResponse();

        when(repository.findAll()).thenReturn(List.of(account1, account2));

        when(mapper.toResponse(account1)).thenReturn(response1);

        when(mapper.toResponse(account2)).thenReturn(response2);

        List<AccountResponse> result = service.getAllAccounts();

        assertEquals(2, result.size());
        assertEquals(response1, result.get(0));
        assertEquals(response2, result.get(1));
    }

    @Test
    void testIncreaseBalance() {
        BigDecimal amount = new BigDecimal("100.00");

        Account account = Account.builder()
                .iban(IBAN)
                .balance(new BigDecimal("500.00"))
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        when(repository.findByIban(IBAN)).thenReturn(Optional.of(account));

        service.increaseBalance(IBAN, amount);

        assertEquals(new BigDecimal("600.00"), account.getBalance());
    }

    @Test
    void testIncreaseBalance_shouldThrowException_whenAccountIsNotActive() {
        BigDecimal amount = new BigDecimal("100.00");

        Account account = Account.builder()
                .iban(IBAN)
                .balance(new BigDecimal("500.00"))
                .accountStatus(AccountStatus.BLOCKED)
                .build();

        when(repository.findByIban(IBAN)).thenReturn(Optional.of(account));

        AccountNotActiveException exception = assertThrows(AccountNotActiveException.class,
                () -> service.increaseBalance(IBAN, amount));

        assertEquals("This account is not active", exception.getMessage());
    }

    @Test
    void testIncreaseBalance_shouldThrowException_whenAmountIsZero() {
        Account account = Account.builder()
                .iban(IBAN)
                .balance(new BigDecimal("500.00"))
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        when(repository.findByIban(IBAN)).thenReturn(Optional.of(account));

        InvalidAmountException exception = assertThrows(
                InvalidAmountException.class,
                () -> service.increaseBalance(IBAN, BigDecimal.ZERO)
        );

        assertEquals("Amount must be greater than zero", exception.getMessage());
    }

    @Test
    void testIncreaseBalance_shouldThrowException_whenAmountIsNull() {
        Account account = Account.builder()
                .iban(IBAN)
                .balance(new BigDecimal("500.00"))
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        when(repository.findByIban(IBAN)).thenReturn(Optional.of(account));

        InvalidAmountException exception = assertThrows(
                InvalidAmountException.class,
                () -> service.increaseBalance(IBAN, null)
        );

        assertEquals("Amount must be greater than zero", exception.getMessage());
    }

    @Test
    void testIncreaseBalance_shouldThrowException_whenAmountIsNegative() {
        Account account = Account.builder()
                .iban(IBAN)
                .balance(new BigDecimal("500.00"))
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        when(repository.findByIban(IBAN)).thenReturn(Optional.of(account));

        InvalidAmountException exception = assertThrows(
                InvalidAmountException.class,
                () -> service.increaseBalance(IBAN, new BigDecimal("-100.00"))
        );

        assertEquals("Amount must be greater than zero", exception.getMessage());
    }

    @Test
    void testDecreaseBalance() {
        BigDecimal amount = new BigDecimal("100.00");

        Account account = Account.builder()
                .iban(IBAN)
                .balance(new BigDecimal("500.00"))
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        when(repository.findByIban(IBAN)).thenReturn(Optional.of(account));

        service.decreaseBalance(IBAN, amount);

        assertEquals(new BigDecimal("400.00"), account.getBalance());
    }

    @Test
    void testDecreaseBalance_shouldThrowException_whenAccountIsNotActive() {
        BigDecimal amount = new BigDecimal("100.00");

        Account account = Account.builder()
                .iban(IBAN)
                .balance(new BigDecimal("500.00"))
                .accountStatus(AccountStatus.BLOCKED)
                .build();

        when(repository.findByIban(IBAN)).thenReturn(Optional.of(account));

        AccountNotActiveException exception = assertThrows(AccountNotActiveException.class,
                () -> service.decreaseBalance(IBAN, amount));

        assertEquals("This account is not active", exception.getMessage());
    }

    @Test
    void testDecreaseBalance_shouldThrowException_whenAmountIsZero() {
        Account account = Account.builder()
                .iban(IBAN)
                .balance(new BigDecimal("500.00"))
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        when(repository.findByIban(IBAN)).thenReturn(Optional.of(account));

        InvalidAmountException exception = assertThrows(
                InvalidAmountException.class,
                () -> service.decreaseBalance(IBAN, BigDecimal.ZERO)
        );

        assertEquals("Amount must be greater than zero", exception.getMessage());
    }

    @Test
    void testDecreaseBalance_shouldThrowException_whenAmountIsNull() {
        Account account = Account.builder()
                .iban(IBAN)
                .balance(new BigDecimal("500.00"))
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        when(repository.findByIban(IBAN)).thenReturn(Optional.of(account));

        InvalidAmountException exception = assertThrows(
                InvalidAmountException.class,
                () -> service.decreaseBalance(IBAN, null)
        );

        assertEquals("Amount must be greater than zero", exception.getMessage());
    }

    @Test
    void testDecreaseBalance_shouldThrowException_whenAmountIsNegative() {
        Account account = Account.builder()
                .iban(IBAN)
                .balance(new BigDecimal("500.00"))
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        when(repository.findByIban(IBAN)).thenReturn(Optional.of(account));

        InvalidAmountException exception = assertThrows(
                InvalidAmountException.class,
                () -> service.decreaseBalance(IBAN, new BigDecimal("-100.00"))
        );

        assertEquals("Amount must be greater than zero", exception.getMessage());
    }

    @Test
    void testDecreaseBalance_shouldThrowException_whenBalanceIsInsufficient() {
        BigDecimal amount = new BigDecimal("600.00");

        Account account = Account.builder()
                .iban(IBAN)
                .balance(new BigDecimal("500.00"))
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        when(repository.findByIban(IBAN)).thenReturn(Optional.of(account));

        InsufficientBalanceException exception = assertThrows(InsufficientBalanceException.class,
                () -> service.decreaseBalance(IBAN, amount));

        assertEquals("Insufficient balance", exception.getMessage());
    }

    @Test
    void testUpdateAccount() {
        UpdateAccountRequest request = new UpdateAccountRequest();

        request.setAccountType(AccountType.SAVINGS);
        request.setAccountStatus(AccountStatus.BLOCKED);

        Account account = Account.builder()
                .id(ACCOUNT_ID)
                .balance(new BigDecimal("500.00"))
                .accountType(AccountType.DEBIT)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        AccountResponse response = new AccountResponse();

        when(repository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));

        when(repository.save(account)).thenReturn(account);

        when(mapper.toResponse(account)).thenReturn(response);

        AccountResponse result = service.updateAccount(ACCOUNT_ID, request);

        assertEquals(response, result);

        assertEquals(
                AccountType.SAVINGS,
                account.getAccountType()
        );

        assertEquals(AccountStatus.BLOCKED,
                account.getAccountStatus());
    }

    @Test
    void updateAccount_shouldThrowException_whenAccountNotFound() {
        UpdateAccountRequest request = new UpdateAccountRequest();

        when(repository.findById(ACCOUNT_ID)).thenReturn(Optional.empty());

        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class,
                () -> service.updateAccount(ACCOUNT_ID, request));

        assertEquals("Account not found", exception.getMessage());
    }

    @Test
    void deleteAccount() {
        Account account = new Account();
        account.setId(ACCOUNT_ID);

        when(repository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));

        service.deleteAccount(ACCOUNT_ID);
    }

    @Test
    void deleteAccount_shouldThrowException_whenAccountNotFound() {
        when(repository.findById(ACCOUNT_ID)).thenReturn(Optional.empty());

        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class,
                () -> service.deleteAccount(ACCOUNT_ID));

        assertEquals("Account not found", exception.getMessage());
    }
}