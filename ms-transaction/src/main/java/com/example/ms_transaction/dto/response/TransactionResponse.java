package com.example.ms_transaction.dto.response;

import com.example.ms_transaction.enums.TransactionStatus;
import com.example.ms_transaction.enums.TransactionType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class TransactionResponse {

    private Long id;
    private String fromIban;
    private String toIban;
    private BigDecimal amount;
    private TransactionType transactionType;
    private TransactionStatus transactionStatus;
    private String description;
    private LocalDateTime createdAt;

}
