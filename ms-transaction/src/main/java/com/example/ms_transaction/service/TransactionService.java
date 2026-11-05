package com.example.ms_transaction.service;

import com.example.ms_transaction.dto.request.DepositRequest;
import com.example.ms_transaction.dto.request.TransferRequest;
import com.example.ms_transaction.dto.request.WithdrawalRequest;
import com.example.ms_transaction.dto.response.TransactionResponse;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface TransactionService {

    TransactionResponse transfer(TransferRequest request, String token);

    TransactionResponse withdrawal(WithdrawalRequest request, String token);

    TransactionResponse deposit(DepositRequest request, String token);

    @PreAuthorize("hasRole('ADMIN')")
    TransactionResponse getTransactionById(Long id);

    @PreAuthorize("hasRole('ADMIN')")
    List<TransactionResponse> getAllTransactions();

    List<TransactionResponse> getTransactionsByIban(String iban, String token);
}