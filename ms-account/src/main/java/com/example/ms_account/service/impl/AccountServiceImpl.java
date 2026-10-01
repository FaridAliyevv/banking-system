package com.example.ms_account.service.impl;

import com.example.ms_account.client.UserClient;
import com.example.ms_account.dto.request.CreateAccountRequest;
import com.example.ms_account.dto.request.UpdateAccountRequest;
import com.example.ms_account.dto.response.AccountResponse;
import com.example.ms_account.entity.Account;
import com.example.ms_account.enums.AccountStatus;
import com.example.ms_account.exception.*;
import com.example.ms_account.mapper.AccountMapper;
import com.example.ms_account.repository.AccountRepository;
import com.example.ms_account.service.AccountService;
import com.example.ms_account.util.IbanGenerator;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository repository;
    private final AccountMapper mapper;
    private final UserClient userClient;

    @Override
    public AccountResponse createAccount(CreateAccountRequest request) {

        try {
            userClient.getUserById(request.getUserId());
        } catch (FeignException.NotFound e) {
            throw new UserNotFoundException("User not found");
        }

        Account account = mapper.toEntity(request);

        account.setIban(IbanGenerator.generate());
        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setBalance(BigDecimal.ZERO);

        Account savedAccount = repository.save(account);

        AccountResponse response = mapper.toResponse(savedAccount);

        return response;
    }

    @Override
    public AccountResponse getAccountById(Long id) {
        Account account = repository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        AccountResponse response = mapper.toResponse(account);

        return response;
    }

    @Override
    public AccountResponse getAccountByIban(String iban) {
        Account account = repository.findByIban(iban)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));
        AccountResponse response = mapper.toResponse(account);

        return response;
    }

    @Override
    public List<AccountResponse> getAllAccountsByUserId(Long userId) {
        return repository.findByUserId(userId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public List<AccountResponse> getAllAccounts() {
        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public void increaseBalance(String iban, BigDecimal amount) {

        Account account = repository.findByIban(iban)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        if (account.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException("This account is not active");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0 ) {
            throw new InvalidAmountException("Amount must be greater than zero");
        }

        account.setBalance(account.getBalance().add(amount));

        repository.save(account);
    }

    @Override
    public void decreaseBalance(String iban, BigDecimal amount) {

        Account account = repository.findByIban(iban)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        if (account.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException("This account is not active");
        }

        if (amount == null ||amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Amount must be greater than zero");
        }

        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        account.setBalance(account.getBalance().subtract(amount));

        repository.save(account);
    }

    @Override
    public AccountResponse updateAccount(Long id, UpdateAccountRequest request) {

        Account account = repository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        account.setAccountType(request.getAccountType());
        account.setAccountStatus(request.getAccountStatus());

        Account updatedAccount = repository.save(account);

        return mapper.toResponse(updatedAccount);
    }

    @Override
    public void deleteAccount(Long accountId) {

        Account account = repository.findById(accountId)
                        .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        repository.delete(account);
    }
}
