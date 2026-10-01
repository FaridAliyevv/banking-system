package com.example.ms_account.dto.request;

import com.example.ms_account.enums.AccountType;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAccountRequest {

    @NotBlank
    private Long userId;

    @NotBlank
    private AccountType accountType;
}
