package com.example.ms_transaction.dto.response;

import com.example.ms_transaction.enums.AccountStatus;

import java.math.BigDecimal;

public record AccountResponse(
        Long id,
        Long userId,
        String iban,
        BigDecimal balance,
        String accountType,
        AccountStatus accountStatus
) {
}
