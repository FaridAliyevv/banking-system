package com.example.ms_account.dto.request;

import com.example.ms_account.enums.AccountStatus;
import com.example.ms_account.enums.AccountType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAccountRequest {

    private AccountType accountType;

    private AccountStatus accountStatus;
}
