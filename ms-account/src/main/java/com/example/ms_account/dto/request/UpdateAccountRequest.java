package com.example.ms_account.dto.request;

import com.example.ms_account.enums.AccountStatus;
import com.example.ms_account.enums.AccountType;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAccountRequest {

    private AccountType accountType;

    private AccountStatus accountStatus;
}
