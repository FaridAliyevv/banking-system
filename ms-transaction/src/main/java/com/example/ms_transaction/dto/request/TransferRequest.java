package com.example.ms_transaction.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TransferRequest {

    @NotBlank
    private String fromIban;

    @NotBlank
    private String toIban;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotBlank
    private String description;
}
