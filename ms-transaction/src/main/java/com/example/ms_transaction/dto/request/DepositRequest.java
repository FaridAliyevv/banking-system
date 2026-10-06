package com.example.ms_transaction.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositRequest {

    @NotBlank
    private String toIban;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotBlank
    private String description;
}
