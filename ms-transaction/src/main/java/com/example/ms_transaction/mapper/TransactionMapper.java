package com.example.ms_transaction.mapper;

import com.example.ms_transaction.dto.request.DepositRequest;
import com.example.ms_transaction.dto.request.TransferRequest;
import com.example.ms_transaction.dto.request.WithdrawalRequest;
import com.example.ms_transaction.dto.response.TransactionResponse;
import com.example.ms_transaction.entity.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public Transaction toEntity(TransferRequest request) {
        Transaction transaction = new Transaction();

        transaction.setFromIban(request.getFromIban());
        transaction.setToIban(request.getToIban());
        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());

        return transaction;
    }

    public Transaction toEntity(DepositRequest request) {
        Transaction transaction = new Transaction();

        transaction.setToIban(request.getToIban());
        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());

        return transaction;
    }

    public Transaction toEntity(WithdrawalRequest request) {
        Transaction transaction = new Transaction();

        transaction.setFromIban(request.getFromIban());
        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());

        return transaction;
    }

    public TransactionResponse toResponse(Transaction transaction) {
        TransactionResponse response = new TransactionResponse();

        response.setId(transaction.getId());
        response.setFromIban(transaction.getFromIban());
        response.setToIban(transaction.getToIban());
        response.setTransactionType(transaction.getTransactionType());
        response.setTransactionStatus(transaction.getTransactionStatus());
        response.setDescription(transaction.getDescription());
        response.setCreatedAt(transaction.getCreatedAt());

        return response;
    }
}
