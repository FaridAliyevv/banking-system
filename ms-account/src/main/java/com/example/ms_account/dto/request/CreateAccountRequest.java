package com.example.ms_account.dto.request;

import com.example.ms_account.enums.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequest {

    @NotNull
    private Long userId;

    @NotBlank
    private AccountType accountType;
}
