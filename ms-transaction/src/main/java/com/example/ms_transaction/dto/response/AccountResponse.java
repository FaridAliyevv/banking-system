package com.example.ms_transaction.dto.response;

import com.example.ms_transaction.enums.AccountStatus;
import com.example.ms_transaction.enums.AccountType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class AccountResponse {

    private Long id;
    private Long userId;
    private String iban;
    private BigDecimal balance;
    private AccountType accountType;
    private AccountStatus accountStatus;

}
