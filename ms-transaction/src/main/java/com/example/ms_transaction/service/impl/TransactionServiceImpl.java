package com.example.ms_transaction.service.impl;

import com.example.ms_transaction.client.AccountClient;
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
import com.example.ms_transaction.security.JwtService;
import com.example.ms_transaction.service.TransactionService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
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
    private final JwtService jwtService;

    @Override
    public TransactionResponse transfer(TransferRequest request, String token) {

        String serviceToken = "Bearer " + jwtService.generateServiceToken();

        AccountResponse fromIban = client.getAccountByIban(request.getFromIban(), token);
        AccountResponse toIban = client.getAccountByIban(request.getToIban(), serviceToken);

        Transaction transaction = mapper.toEntity(request);

        transaction.setTransactionType(TransactionType.TRANSFER);
        transaction.setCreatedAt(LocalDateTime.now());

        if (fromIban.getAccountStatus() != AccountStatus.ACTIVE) {

            transaction.setTransactionStatus(TransactionStatus.FAILED);
            repository.save(transaction);

            throw new InactiveAccountException("Source account is not active");
        }

        if (toIban.getAccountStatus() != AccountStatus.ACTIVE) {

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

        if (fromIban.getBalance().compareTo(request.getAmount()) < 0) {

            transaction.setTransactionStatus(TransactionStatus.FAILED);
            repository.save(transaction);

            throw new InsufficientBalanceException("Insufficient balance");
        }

        client.decreaseBalance(
                request.getFromIban(),
                request.getAmount(),
                serviceToken
        );

        client.increaseBalance(
                request.getToIban(),
                request.getAmount(),
                serviceToken
        );

        transaction.setTransactionStatus(TransactionStatus.COMPLETED);

        Transaction savedTransaction = repository.save(transaction);
        TransactionResponse response = mapper.toResponse(savedTransaction);

        return response;
    }

    @Override
    public TransactionResponse deposit(DepositRequest request, String token) {

        String serviceToken = "Bearer " + jwtService.generateServiceToken();

        AccountResponse account = getOwnAccount(request.getToIban(), token);
        Transaction transaction = mapper.toEntity(request);
        transaction.setTransactionType(TransactionType.DEPOSIT);
        transaction.setCreatedAt(LocalDateTime.now());

        if (account.getAccountStatus() != AccountStatus.ACTIVE) {

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
                request.getAmount(),
                serviceToken
        );

        transaction.setTransactionStatus(TransactionStatus.COMPLETED);

        Transaction savedTransaction = repository.save(transaction);
        TransactionResponse response = mapper.toResponse(savedTransaction);

        return response;
    }

    @Override
    public TransactionResponse withdrawal(WithdrawalRequest request, String token) {

        String serviceToken = "Bearer " + jwtService.generateServiceToken();

        AccountResponse account = getOwnAccount(request.getFromIban(), token);

        Transaction transaction = mapper.toEntity(request);
        transaction.setTransactionType(TransactionType.WITHDRAWAL);
        transaction.setCreatedAt(LocalDateTime.now());

        if (account.getAccountStatus() != AccountStatus.ACTIVE) {

            transaction.setTransactionStatus(TransactionStatus.FAILED);
            repository.save(transaction);

            throw new InactiveAccountException("This account is not active");
        }

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {

            transaction.setTransactionStatus(TransactionStatus.FAILED);
            repository.save(transaction);

            throw new InvalidAmountException("Amount must be greater than zero");
        }

        if (account.getBalance().compareTo(request.getAmount()) < 0) {

            transaction.setTransactionStatus(TransactionStatus.FAILED);
            repository.save(transaction);

            throw new InsufficientBalanceException("Insufficient balance");
        }

        client.decreaseBalance(
                request.getFromIban(),
                request.getAmount(),
                serviceToken
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
    public List<TransactionResponse> getTransactionsByIban(String iban, String token) {

        getOwnAccount(iban, token);

        List<Transaction> transactions = repository.findByFromIbanOrToIban(iban, iban);

        return transactions.stream()
                .map(mapper::toResponse)
                .toList();
    }

    private AccountResponse getOwnAccount(String iban, String token) {
        try {
            return client.getAccountByIban(iban, token);
        } catch (FeignException e) {
            throw new AccessDeniedException("Access denied");
        }
    }
}
