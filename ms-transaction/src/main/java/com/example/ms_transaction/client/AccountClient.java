package com.example.ms_transaction.client;

import com.example.ms_transaction.dto.response.AccountResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@FeignClient(
        name = "ms-account",
        url = "${account-service.url}"
)
public interface AccountClient {

    @GetMapping("/accounts/{id}")
    AccountResponse getAccountById(@PathVariable Long id);

    @GetMapping("/accounts/iban/{iban}")
    AccountResponse getAccountByIban(@PathVariable String iban,
                                     @RequestHeader("Authorization") String token);

    @PutMapping("/accounts/iban/{iban}/increase")
    void increaseBalance(
            @PathVariable String iban,
            @RequestParam BigDecimal amount,
            @RequestHeader("Authorization") String token
    );

    @PutMapping("/accounts/iban/{iban}/decrease")
    void decreaseBalance(
            @PathVariable String iban,
            @RequestParam BigDecimal amount,
            @RequestHeader("Authorization") String token
    );

    @PutMapping("/accounts/{id}/withdraw")
    AccountResponse withdraw(
            @PathVariable Long id,
            @RequestParam BigDecimal amount
    );

    @PutMapping("/accounts/{id}/deposit")
    AccountResponse deposit(
            @PathVariable Long id,
            @RequestParam BigDecimal amount
    );
}
