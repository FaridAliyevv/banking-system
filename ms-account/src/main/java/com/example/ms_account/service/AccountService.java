package com.example.ms_account.service;

import com.example.ms_account.dto.request.CreateAccountRequest;
import com.example.ms_account.dto.request.UpdateAccountRequest;
import com.example.ms_account.dto.response.AccountResponse;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {

    AccountResponse createAccount(CreateAccountRequest request, String token);

    @PostAuthorize("hasRole('ADMIN') or returnObject.userId == authentication.principal")
    AccountResponse getAccountById(Long id);

    @PostAuthorize("hasAnyRole('ADMIN','SERVICE') or returnObject.userId == authentication.principal")
    AccountResponse getAccountByIban(String iban);

    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal")
    List<AccountResponse> getAllAccountsByUserId(Long userId);

    @PreAuthorize("hasRole('ADMIN')")
    List<AccountResponse> getAllAccounts();

    @PreAuthorize("hasAnyRole('ADMIN','SERVICE')")
    void increaseBalance(String iban, BigDecimal amount);

    @PreAuthorize("hasAnyRole('ADMIN','SERVICE')")
    void decreaseBalance(String iban, BigDecimal amount);

    @PreAuthorize("hasRole('ADMIN')")
    AccountResponse updateAccount(Long id, UpdateAccountRequest request);

    @PreAuthorize("hasRole('ADMIN')")
    void deleteAccount(Long id);
}
