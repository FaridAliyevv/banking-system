package com.example.ms_account.controller;

import com.example.ms_account.dto.request.CreateAccountRequest;
import com.example.ms_account.dto.request.UpdateAccountRequest;
import com.example.ms_account.dto.response.AccountResponse;
import com.example.ms_account.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public AccountResponse createAccount(@RequestBody CreateAccountRequest request) {
        return accountService.createAccount(request);
    }

    @GetMapping("/{id}")
    public AccountResponse getAccountById(@PathVariable Long id) {
        return accountService.getAccountById(id);
    }

    @GetMapping("/iban/{iban}")
    public AccountResponse getAccountByIban(@PathVariable String iban) {
        return accountService.getAccountByIban(iban);
    }

    @GetMapping
    public List<AccountResponse> getAllAccounts() {
        return accountService.getAllAccounts();
    }

    @GetMapping("/user/{userId}")
    public List<AccountResponse> getAllAccountsByUserId(@PathVariable Long userId) {
        return accountService.getAllAccountsByUserId(userId);
    }

    @PutMapping("/iban/{iban}/increase")
    public void increaseBalance(
            @PathVariable String iban,
            @RequestParam BigDecimal amount) {

        accountService.increaseBalance(iban, amount);
    }

    @PutMapping("/iban/{iban}/decrease")
    public void decreaseBalance(
            @PathVariable String iban,
            @RequestParam BigDecimal amount) {

        accountService.decreaseBalance(iban, amount);
    }

    @PutMapping("/{id}")
    public AccountResponse updateAccount(@PathVariable Long id,
                                         @RequestBody UpdateAccountRequest request) {

        return accountService.updateAccount(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteAccount(@PathVariable Long id) {
        accountService.deleteAccount(id);
    }
}
