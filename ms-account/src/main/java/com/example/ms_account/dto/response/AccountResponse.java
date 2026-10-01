package com.example.ms_account.dto.response;

import com.example.ms_account.enums.AccountStatus;
import com.example.ms_account.enums.AccountType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AccountResponse {

    private Long id;

    private Long userId;

    private String iban;

    private BigDecimal balance;

    private AccountType accountType;

    private AccountStatus accountStatus;
}
