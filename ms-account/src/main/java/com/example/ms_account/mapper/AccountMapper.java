package com.example.ms_account.mapper;

import com.example.ms_account.dto.request.CreateAccountRequest;
import com.example.ms_account.dto.response.AccountResponse;
import com.example.ms_account.entity.Account;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public Account toEntity(CreateAccountRequest request) {
        Account account = new Account();

        account.setUserId(request.getUserId());
        account.setAccountType(request.getAccountType());

        return account;
    }

    public AccountResponse toResponse(Account account) {
        AccountResponse response = new AccountResponse();

        response.setId(account.getId());
        response.setUserId(account.getUserId());
        response.setIban(account.getIban());
        response.setBalance(account.getBalance());
        response.setAccountType(account.getAccountType());
        response.setAccountStatus(account.getAccountStatus());

        return response;
    }
}
