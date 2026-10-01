package com.example.ms_account.service.impl;

import com.example.ms_account.client.UserClient;
import com.example.ms_account.dto.request.CreateAccountRequest;
import com.example.ms_account.dto.request.UpdateAccountRequest;
import com.example.ms_account.dto.response.AccountResponse;
import com.example.ms_account.dto.response.UserResponse;
import com.example.ms_account.entity.Account;
import com.example.ms_account.enums.AccountStatus;
import com.example.ms_account.enums.AccountType;
import com.example.ms_account.exception.*;
import com.example.ms_account.mapper.AccountMapper;
import com.example.ms_account.repository.AccountRepository;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

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
        request.setUserId(1L);
        request.setAccountType(AccountType.DEBIT);

        Account account = new Account();
        Account savedAccount = new Account();
        AccountResponse response = new AccountResponse();

        when(client.getUserById(1L)).thenReturn(new UserResponse(
                1L,
                "John",
                "Smith",
                "john@gmail.com",
                "12345678",
                "USER"
        ));

        when(mapper.toEntity(request)).thenReturn(account);

        when(repository.save(account)).thenReturn(savedAccount);

        when(mapper.toResponse(savedAccount)).thenReturn(response);

        AccountResponse result = service.createAccount(request);

        assertEquals(response, result);
    }

    @Test
    void testCreateAccount_shouldThrowException_whenUserNotFound() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setUserId(1L);

        FeignException.NotFound feignException = mock(FeignException.NotFound.class);

        when(client.getUserById(1L)).thenThrow(feignException);

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> service.createAccount(request));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testGetAccountById() {
        Long accountId = 1L;

        Account account = new Account();
        account.setId(accountId);

        AccountResponse response = new AccountResponse();

        when(repository.findById(accountId)).thenReturn(Optional.of(account));

        when(mapper.toResponse(account)).thenReturn(response);

        AccountResponse result = service.getAccountById(accountId);

        assertNotNull(result);
        assertEquals(response, result);
    }

    @Test
    void testGetAccountById_shouldThrowException_whenAccountNotFound() {
        Long accountId = 1L;

        when(repository.findById(accountId)).thenReturn(Optional.empty());

        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class,
                () -> service.getAccountById(accountId));

        assertEquals("Account not found", exception.getMessage());
    }

    @Test
    void testGetAccountByIban() {
        String iban = "AZ90 QRYR Z4W2 KBDS 4RD0 9Z9B SFFA";

        Account account = new Account();
        account.setIban(iban);

        AccountResponse response = new AccountResponse();

        when(repository.findByIban(iban)).thenReturn(Optional.of(account));

        when(mapper.toResponse(account)).thenReturn(response);

        AccountResponse result = service.getAccountByIban(iban);

        assertNotNull(result);
        assertEquals(response, result);
    }

    @Test
    void testGetAccountByIban_shouldThrowException_whenAccountNotFound() {
        String iban = "AZ90 QRYR Z4W2 KBDS 4RD0 9Z9B SFFA";

        when(repository.findByIban(iban)).thenReturn(Optional.empty());

        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class,
                () -> service.getAccountByIban(iban));

        assertEquals("Account not found", exception.getMessage());
    }

    @Test
    void testGetAllAccountsByUserId() {
        Long userId = 1L;

        Account account1 = new Account();
        Account account2 = new Account();

        AccountResponse response1 = new AccountResponse();
        AccountResponse response2 = new AccountResponse();

        when(repository.findByUserId(userId)).thenReturn(List.of(account1, account2));

        when(mapper.toResponse(account1)).thenReturn(response1);

        when(mapper.toResponse(account2)).thenReturn(response2);

        List<AccountResponse> result = service.getAllAccountsByUserId(userId);

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
        String iban = "AZ90 QRYR Z4W2 KBDS 4RD0 9Z9B SFFA";
        BigDecimal amount = new BigDecimal("100.00");

        Account account = new Account();
        account.setIban(iban);
        account.setBalance(new BigDecimal("500.00"));
        account.setAccountStatus(AccountStatus.ACTIVE);

        when(repository.findByIban(iban)).thenReturn(Optional.of(account));

        service.increaseBalance(iban, amount);

        assertEquals(new BigDecimal("600.00"), account.getBalance());
    }

    @Test
    void testIncreaseBalance_shouldThrowException_whenAccountIsNotActive() {
        String iban = "AZ90 QRYR Z4W2 KBDS 4RD0 9Z9B SFFA";
        BigDecimal amount = new BigDecimal("100.00");

        Account account = new Account();
        account.setIban(iban);
        account.setBalance(new BigDecimal("500.00"));
        account.setAccountStatus(AccountStatus.BLOCKED);

        when(repository.findByIban(iban)).thenReturn(Optional.of(account));

        AccountNotActiveException exception = assertThrows(AccountNotActiveException.class,
                () -> service.increaseBalance(iban, amount));

        assertEquals("This account is not active", exception.getMessage());
    }

    @Test
    void testIncreaseBalance_shouldThrowException_whenAmountIsZero() {
        String iban = "AZ90 QRYR Z4W2 KBDS 4RD0 9Z9B SFFA";

        Account account = new Account();
        account.setIban(iban);
        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setBalance(new BigDecimal("500.00"));

        when(repository.findByIban(iban)).thenReturn(Optional.of(account));

        InvalidAmountException exception = assertThrows(
                InvalidAmountException.class,
                () -> service.increaseBalance(iban, BigDecimal.ZERO)
        );

        assertEquals("Amount must be greater than zero", exception.getMessage());
    }

    @Test
    void testIncreaseBalance_shouldThrowException_whenAmountIsNull() {
        String iban = "AZ90 QRYR Z4W2 KBDS 4RD0 9Z9B SFFA";

        Account account = new Account();
        account.setIban(iban);
        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setBalance(new BigDecimal("500.00"));

        when(repository.findByIban(iban)).thenReturn(Optional.of(account));

        InvalidAmountException exception = assertThrows(
                InvalidAmountException.class,
                () -> service.increaseBalance(iban, null)
        );

        assertEquals("Amount must be greater than zero", exception.getMessage());
    }

    @Test
    void testIncreaseBalance_shouldThrowException_whenAmountIsNegative() {
        String iban = "AZ90 QRYR Z4W2 KBDS 4RD0 9Z9B SFFA";

        Account account = new Account();
        account.setIban(iban);
        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setBalance(new BigDecimal("500.00"));

        when(repository.findByIban(iban)).thenReturn(Optional.of(account));

        InvalidAmountException exception = assertThrows(
                InvalidAmountException.class,
                () -> service.increaseBalance(iban, new BigDecimal("-100.00"))
        );

        assertEquals("Amount must be greater than zero", exception.getMessage());
    }

    @Test
    void testDecreaseBalance() {
        String iban = "AZ90 QRYR Z4W2 KBDS 4RD0 9Z9B SFFA";
        BigDecimal amount = new BigDecimal("100.00");

        Account account = new Account();
        account.setIban(iban);
        account.setBalance(new BigDecimal("500.00"));
        account.setAccountStatus(AccountStatus.ACTIVE);

        when(repository.findByIban(iban)).thenReturn(Optional.of(account));

        service.decreaseBalance(iban, amount);

        assertEquals(new BigDecimal("400.00"), account.getBalance());
    }

    @Test
    void testDecreaseBalance_shouldThrowException_whenAccountIsNotActive() {
        String iban = "AZ90 QRYR Z4W2 KBDS 4RD0 9Z9B SFFA";
        BigDecimal amount = new BigDecimal("100.00");

        Account account = new Account();
        account.setIban(iban);
        account.setBalance(new BigDecimal("500.00"));
        account.setAccountStatus(AccountStatus.BLOCKED);

        when(repository.findByIban(iban)).thenReturn(Optional.of(account));

        AccountNotActiveException exception = assertThrows(AccountNotActiveException.class,
                () -> service.decreaseBalance(iban, amount));

        assertEquals("This account is not active", exception.getMessage());
    }

    @Test
    void testDecreaseBalance_shouldThrowException_whenAmountIsZero() {
        String iban = "AZ90 QRYR Z4W2 KBDS 4RD0 9Z9B SFFA";

        Account account = new Account();
        account.setIban(iban);
        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setBalance(new BigDecimal("500.00"));

        when(repository.findByIban(iban)).thenReturn(Optional.of(account));

        InvalidAmountException exception = assertThrows(
                InvalidAmountException.class,
                () -> service.decreaseBalance(iban, BigDecimal.ZERO)
        );

        assertEquals("Amount must be greater than zero", exception.getMessage());
    }

    @Test
    void testDecreaseBalance_shouldThrowException_whenAmountIsNull() {
        String iban = "AZ90 QRYR Z4W2 KBDS 4RD0 9Z9B SFFA";

        Account account = new Account();
        account.setIban(iban);
        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setBalance(new BigDecimal("500.00"));

        when(repository.findByIban(iban)).thenReturn(Optional.of(account));

        InvalidAmountException exception = assertThrows(
                InvalidAmountException.class,
                () -> service.decreaseBalance(iban, null)
        );

        assertEquals("Amount must be greater than zero", exception.getMessage());
    }

    @Test
    void testDecreaseBalance_shouldThrowException_whenAmountIsNegative() {
        String iban = "AZ90 QRYR Z4W2 KBDS 4RD0 9Z9B SFFA";

        Account account = new Account();
        account.setIban(iban);
        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setBalance(new BigDecimal("500.00"));

        when(repository.findByIban(iban)).thenReturn(Optional.of(account));

        InvalidAmountException exception = assertThrows(
                InvalidAmountException.class,
                () -> service.decreaseBalance(iban, new BigDecimal("-100.00"))
        );

        assertEquals("Amount must be greater than zero", exception.getMessage());
    }

    @Test
    void testDecreaseBalance_shouldThrowException_whenBalanceIsInsufficient() {
        String iban = "AZ90 QRYR Z4W2 KBDS 4RD0 9Z9B SFFA";
        BigDecimal amount = new BigDecimal("600.00");

        Account account = new Account();
        account.setIban(iban);
        account.setBalance(new BigDecimal("500.00"));
        account.setAccountStatus(AccountStatus.ACTIVE);

        when(repository.findByIban(iban)).thenReturn(Optional.of(account));

        InsufficientBalanceException exception = assertThrows(InsufficientBalanceException.class,
                () -> service.decreaseBalance(iban, amount));

        assertEquals("Insufficient balance", exception.getMessage());
    }

    @Test
    void testUpdateAccount() {
        Long accountId = 1L;
        UpdateAccountRequest request = new UpdateAccountRequest();

        request.setAccountType(AccountType.SAVINGS);
        request.setAccountStatus(AccountStatus.BLOCKED);

        Account account = new Account();
        account.setId(accountId);
        account.setAccountType(AccountType.DEBIT);
        account.setAccountStatus(AccountStatus.ACTIVE);

        AccountResponse response = new AccountResponse();

        when(repository.findById(accountId)).thenReturn(Optional.of(account));

        when(repository.save(account)).thenReturn(account);

        when(mapper.toResponse(account)).thenReturn(response);

        AccountResponse result = service.updateAccount(accountId, request);

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
        Long accountId = 1L;

        UpdateAccountRequest request = new UpdateAccountRequest();

        when(repository.findById(accountId)).thenReturn(Optional.empty());

        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class,
                () -> service.updateAccount(accountId, request));

        assertEquals("Account not found", exception.getMessage());
    }

    @Test
    void deleteAccount() {
        Long accountId = 1L;

        Account account = new Account();
        account.setId(accountId);

        when(repository.findById(accountId)).thenReturn(Optional.of(account));

        service.deleteAccount(accountId);
    }

    @Test
    void deleteAccount_shouldThrowException_whenAccountNotFound() {
        Long accountId = 1L;

        when(repository.findById(accountId)).thenReturn(Optional.empty());

        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class,
                () -> service.deleteAccount(accountId));

        assertEquals("Account not found", exception.getMessage());
    }

}