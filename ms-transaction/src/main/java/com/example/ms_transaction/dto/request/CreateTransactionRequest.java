package com.example.ms_transaction.dto.request;

import com.example.ms_transaction.enums.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateTransactionRequest {

    private String fromIban;

    private String toIban;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotBlank
    private String description;
}
