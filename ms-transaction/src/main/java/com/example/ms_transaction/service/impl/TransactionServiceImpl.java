package com.example.ms_transaction.service.impl;

import com.example.ms_transaction.client.AccountClient;
import com.example.ms_transaction.dto.request.CreateTransactionRequest;
import com.example.ms_transaction.dto.request.DepositRequest;
import com.example.ms_transaction.dto.request.TransferRequest;
import com.example.ms_transaction.dto.request.WithdrawalRequest;
import com.example.ms_transaction.dto.response.AccountResponse;
import com.example.ms_transaction.dto.response.TransactionResponse;
import com.example.ms_transaction.entity.Transaction;
import com.example.ms_transaction.enums.AccountStatus;
import com.example.ms_transaction.enums.TransactionStatus;
import com.example.ms_transaction.enums.TransactionType;
import com.example.ms_transaction.exception.*;
import com.example.ms_transaction.mapper.TransactionMapper;
import com.example.ms_transaction.repository.TransactionRepository;
import com.example.ms_transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository repository;
    private final TransactionMapper mapper;
    private final AccountClient client;

    @Override
    public TransactionResponse transfer(TransferRequest request) {

        AccountResponse fromIban = client.getAccountByIban(request.getFromIban());
        AccountResponse toIban = client.getAccountByIban(request.getToIban());

        Transaction transaction = mapper.toEntity(request);

        transaction.setTransactionType(TransactionType.TRANSFER);
        transaction.setCreatedAt(LocalDateTime.now());

        if (fromIban.accountStatus() != AccountStatus.ACTIVE) {

            transaction.setTransactionStatus(TransactionStatus.FAILED);
            repository.save(transaction);

            throw new InactiveAccountException("Source account is not active");
        }

        if (toIban.accountStatus() != AccountStatus.ACTIVE) {

            transaction.setTransactionStatus(TransactionStatus.FAILED);
            repository.save(transaction);

            throw new InactiveAccountException("Destination account is not active");
        }

        if (request.getFromIban().equals(request.getToIban())) {

            transaction.setTransactionStatus(TransactionStatus.FAILED);
            repository.save(transaction);

            throw new InvalidTransactionException("Source and destination must be different");
        }

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {

            transaction.setTransactionStatus(TransactionStatus.FAILED);
            repository.save(transaction);

            throw new InvalidAmountException("Amount must be greater than zero");
        }

        if (fromIban.balance().compareTo(request.getAmount()) <= 0) {

            transaction.setTransactionStatus(TransactionStatus.FAILED);
            repository.save(transaction);

            throw new InsufficientBalanceException("Insufficient balance");
        }

        client.decreaseBalance(
                request.getFromIban(),
                request.getAmount()
        );

        client.increaseBalance(
                request.getToIban(),
                request.getAmount()
        );

        transaction.setTransactionStatus(TransactionStatus.COMPLETED);

        Transaction savedTransaction = repository.save(transaction);
        TransactionResponse response = mapper.toResponse(savedTransaction);

        return response;
    }

    @Override
    public TransactionResponse deposit(DepositRequest request) {

        AccountResponse account = client.getAccountByIban(request.getToIban());
        Transaction transaction = mapper.toEntity(request);
        transaction.setTransactionType(TransactionType.DEPOSIT);
        transaction.setCreatedAt(LocalDateTime.now());

        if (account.accountStatus() != AccountStatus.ACTIVE) {

            transaction.setTransactionStatus(TransactionStatus.FAILED);
            repository.save(transaction);

            throw new InactiveAccountException("This account is not active");
        }

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {

            transaction.setTransactionStatus(TransactionStatus.FAILED);
            repository.save(transaction);

            throw new InvalidAmountException("Amount must be greater than zero");
        }

        client.increaseBalance(
                request.getToIban(),
                request.getAmount()
        );

        transaction.setTransactionStatus(TransactionStatus.COMPLETED);

        Transaction savedTransaction = repository.save(transaction);
        TransactionResponse response = mapper.toResponse(savedTransaction);

        return response;
    }

    @Override
    public TransactionResponse withdrawal(WithdrawalRequest request) {

        AccountResponse account = client.getAccountByIban(request.getFromIban());

        Transaction transaction = mapper.toEntity(request);
        transaction.setTransactionType(TransactionType.WITHDRAWAL);
        transaction.setCreatedAt(LocalDateTime.now());

        if (account.accountStatus() != AccountStatus.ACTIVE) {

            transaction.setTransactionStatus(TransactionStatus.FAILED);
            repository.save(transaction);

            throw new InactiveAccountException("This account is not active");
        }

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {

            transaction.setTransactionStatus(TransactionStatus.FAILED);
            repository.save(transaction);

            throw new InvalidAmountException("Amount must be greater than zero");
        }

        if (account.balance().compareTo(request.getAmount()) <= 0) {

            transaction.setTransactionStatus(TransactionStatus.FAILED);
            repository.save(transaction);

            throw new InsufficientBalanceException("Insufficient balance");
        }

        client.decreaseBalance(
                request.getFromIban(),
                request.getAmount()
        );

        transaction.setTransactionStatus(TransactionStatus.COMPLETED);

        Transaction savedTransaction = repository.save(transaction);

        TransactionResponse response = mapper.toResponse(savedTransaction);

        return response;
    }

    @Override
    public TransactionResponse getTransactionById(Long id) {
        Transaction transaction = repository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found"));

        TransactionResponse response = mapper.toResponse(transaction);

        return response;
    }

    @Override
    public List<TransactionResponse> getAllTransactions() {
        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public List<TransactionResponse> getTransactionsByIban(String iban) {

        List<Transaction> transactions = repository.findByFromIbanOrToIban(iban, iban);

        return transactions.stream()
                .map(mapper::toResponse)
                .toList();
    }
}
