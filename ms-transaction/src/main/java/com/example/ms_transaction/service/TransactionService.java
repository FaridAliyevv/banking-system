package com.example.ms_transaction.service;

import com.example.ms_transaction.dto.request.DepositRequest;
import com.example.ms_transaction.dto.request.TransferRequest;
import com.example.ms_transaction.dto.request.WithdrawalRequest;
import com.example.ms_transaction.dto.response.TransactionResponse;

import java.util.List;

public interface TransactionService {

    TransactionResponse transfer(TransferRequest request);

    TransactionResponse withdrawal(WithdrawalRequest request);

    TransactionResponse deposit(DepositRequest request);

    TransactionResponse getTransactionById(Long id);

    List<TransactionResponse> getAllTransactions();

    List<TransactionResponse> getTransactionsByIban(String iban);
}
