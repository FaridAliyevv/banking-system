package com.example.ms_transaction.controller;

import com.example.ms_transaction.dto.request.DepositRequest;
import com.example.ms_transaction.dto.request.TransferRequest;
import com.example.ms_transaction.dto.request.WithdrawalRequest;
import com.example.ms_transaction.dto.response.TransactionResponse;
import com.example.ms_transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transfer")
    public TransactionResponse transfer(@Valid @RequestBody TransferRequest request,
                                        @RequestHeader("Authorization") String token) {
        return transactionService.transfer(request, token);
    }

    @PostMapping("/deposit")
    public TransactionResponse deposit(@Valid @RequestBody DepositRequest request,
                                       @RequestHeader("Authorization") String token) {
        return transactionService.deposit(request, token);
    }

    @PostMapping("/withdrawal")
    public TransactionResponse withdrawal(@Valid @RequestBody WithdrawalRequest request,
                                          @RequestHeader("Authorization") String token) {
        return transactionService.withdrawal(request, token);
    }

    @GetMapping("/{id}")
    public TransactionResponse getTransactionById(@PathVariable Long id) {
        return transactionService.getTransactionById(id);
    }

    @GetMapping("/account/{iban}")
    public List<TransactionResponse> getTransactionsByIban(@PathVariable String iban,
                                                           @RequestHeader("Authorization") String token) {
        return transactionService.getTransactionsByIban(iban, token);
    }

    @GetMapping
    public List<TransactionResponse> getAllTransactions() {
        return transactionService.getAllTransactions();
    }
}
