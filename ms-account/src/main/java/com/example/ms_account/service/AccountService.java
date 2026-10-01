package com.example.ms_account.service;

import com.example.ms_account.dto.request.CreateAccountRequest;
import com.example.ms_account.dto.request.UpdateAccountRequest;
import com.example.ms_account.dto.response.AccountResponse;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {

    AccountResponse createAccount(CreateAccountRequest request);

    AccountResponse getAccountById(Long id);

    AccountResponse getAccountByIban(String iban);

    List<AccountResponse> getAllAccountsByUserId(Long userId);

    List<AccountResponse> getAllAccounts();

    void increaseBalance(String iban, BigDecimal amount);

    void decreaseBalance(String iban, BigDecimal amount);

    AccountResponse updateAccount(Long id, UpdateAccountRequest request);

    void deleteAccount(Long id);
}
