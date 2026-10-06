package com.example.ms_transaction.service.impl;

import com.example.ms_transaction.client.AccountClient;
import com.example.ms_transaction.dto.request.DepositRequest;
import com.example.ms_transaction.dto.request.TransferRequest;
import com.example.ms_transaction.dto.request.WithdrawalRequest;
import com.example.ms_transaction.dto.response.AccountResponse;
import com.example.ms_transaction.dto.response.TransactionResponse;
import com.example.ms_transaction.entity.Transaction;
import com.example.ms_transaction.enums.AccountStatus;
import com.example.ms_transaction.enums.AccountType;
import com.example.ms_transaction.enums.TransactionStatus;
import com.example.ms_transaction.enums.TransactionType;
import com.example.ms_transaction.exception.*;
import com.example.ms_transaction.mapper.TransactionMapper;
import com.example.ms_transaction.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository repository;

    @Mock
    private TransactionMapper mapper;

    @Mock
    private AccountClient client;

    @InjectMocks
    private TransactionServiceImpl service;

    @Test
    void testTransfer() {

        TransferRequest request = TransferRequest.builder()
                .fromIban("AZ90 BHFE J8G3 LOCV 3EQS 4M1A CVFD")
                .toIban("AZ90 QRYR Z4W2 KBDS 4RD0 9Z9B SFFA")
                .amount(new BigDecimal("100.00"))
                .description("transfer description")
                .build();

        AccountResponse fromAccount = AccountResponse.builder()
                .id(1L)
                .userId(1L)
                .iban("AZ90 BHFE J8G3 LOCV 3EQS 4M1A CVFD")
                .balance(new BigDecimal("500.00"))
                .accountType(AccountType.DEBIT)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        AccountResponse toAccount = AccountResponse.builder()
                .id(2L)
                .userId(2L)
                .iban("AZ90 QRYR Z4W2 KBDS 4RD0 9Z9B SFFA")
                .balance(new BigDecimal("700.00"))
                .accountType(AccountType.DEBIT)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        Transaction transaction = new Transaction();
        Transaction savedTransaction = new Transaction();

        TransactionResponse response = new TransactionResponse();

        when(client.getAccountByIban("AZ90 BHFE J8G3 LOCV 3EQS 4M1A CVFD")).thenReturn(fromAccount);

        when(client.getAccountByIban("AZ90 QRYR Z4W2 KBDS 4RD0 9Z9B SFFA")).thenReturn(toAccount);

        when(mapper.toEntity(request)).thenReturn(transaction);

        when(repository.save(transaction)).thenReturn(savedTransaction);

        when(mapper.toResponse(savedTransaction)).thenReturn(response);

        TransactionResponse result = service.transfer(request);

        assertEquals(response, result);
        assertEquals(TransactionType.TRANSFER, transaction.getTransactionType());
        assertEquals(TransactionStatus.COMPLETED, transaction.getTransactionStatus());
    }

    @Test
    void testTransfer_shouldThrowException_whenSourceAccountNotActive() {

        TransferRequest request = TransferRequest.builder()
                .fromIban("AZ90 BHFE J8G3 LOCV 3EQS 4M1A CVFD")
                .toIban("AZ90 QRYR Z4W2 KBDS 4RD0 9Z9B SFFA")
                .amount(new BigDecimal("100.00"))
                .description("transfer description")
                .build();

        AccountResponse fromAccount = AccountResponse.builder()
                .id(1L)
                .userId(1L)
                .iban("AZ90 BHFE J8G3 LOCV 3EQS 4M1A CVFD")
                .balance(new BigDecimal("500.00"))
                .accountType(AccountType.DEBIT)
                .accountStatus(AccountStatus.BLOCKED)
                .build();

        AccountResponse toAccount = AccountResponse.builder()
                .id(2L)
                .userId(2L)
                .iban("AZ90 QRYR Z4W2 KBDS 4RD0 9Z9B SFFA")
                .balance(new BigDecimal("700.00"))
                .accountType(AccountType.DEBIT)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        Transaction transaction = new Transaction();

        when(client.getAccountByIban("AZ90 BHFE J8G3 LOCV 3EQS 4M1A CVFD")).thenReturn(fromAccount);

        when(client.getAccountByIban("AZ90 QRYR Z4W2 KBDS 4RD0 9Z9B SFFA")).thenReturn(toAccount);

        when(mapper.toEntity(request)).thenReturn(transaction);

        when(repository.save(transaction)).thenReturn(transaction);

        InactiveAccountException exception = assertThrows(InactiveAccountException.class,
                () -> service.transfer(request));

        assertEquals(TransactionStatus.FAILED, transaction.getTransactionStatus());
        assertEquals("Source account is not active", exception.getMessage());
    }

    @Test
    void testTransfer_shouldThrowException_whenDestinationAccountNotActive() {

        TransferRequest request = TransferRequest.builder()
                .fromIban("AZ90 BHFE J8G3 LOCV 3EQS 4M1A CVFD")
                .toIban("AZ90 QRYR Z4W2 KBDS 4RD0 9Z9B SFFA")
                .amount(new BigDecimal("100.00"))
                .description("transfer description")
                .build();

        AccountResponse fromAccount = AccountResponse.builder()
                .id(1L)
                .userId(1L)
                .iban("AZ90 BHFE J8G3 LOCV 3EQS 4M1A CVFD")
                .balance(new BigDecimal("500.00"))
                .accountType(AccountType.DEBIT)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        AccountResponse toAccount = AccountResponse.builder()
                .id(2L)
                .userId(2L)
                .iban("AZ90 QRYR Z4W2 KBDS 4RD0 9Z9B SFFA")
                .balance(new BigDecimal("700.00"))
                .accountType(AccountType.DEBIT)
                .accountStatus(AccountStatus.BLOCKED)
                .build();

        Transaction transaction = new Transaction();

        when(client.getAccountByIban("AZ90 BHFE J8G3 LOCV 3EQS 4M1A CVFD")).thenReturn(fromAccount);

        when(client.getAccountByIban("AZ90 QRYR Z4W2 KBDS 4RD0 9Z9B SFFA")).thenReturn(toAccount);

        when(mapper.toEntity(request)).thenReturn(transaction);

        when(repository.save(transaction)).thenReturn(transaction);

        InactiveAccountException exception = assertThrows(InactiveAccountException.class,
                () -> service.transfer(request));

        assertEquals(TransactionStatus.FAILED, transaction.getTransactionStatus());
        assertEquals("Destination account is not active", exception.getMessage());
    }

    @Test
    void testTransfer_shouldThrowException_whenSourceAndDestinationAreEqual() {

        TransferRequest request = TransferRequest.builder()
                .fromIban("AZ90 QRYR Z4W2 KBDS 4RD0 9Z9B SFFA")
                .toIban("AZ90 QRYR Z4W2 KBDS 4RD0 9Z9B SFFA")
                .amount(new BigDecimal("100.00"))
                .description("transfer description")
                .build();

        AccountResponse fromAccount = AccountResponse.builder()
                .id(1L)
                .userId(1L)
                .iban("AZ90 QRYR Z4W2 KBDS 4RD0 9Z9B SFFA")
                .balance(new BigDecimal("500.00"))
                .accountType(AccountType.DEBIT)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        AccountResponse toAccount = AccountResponse.builder()
                .id(2L)
                .userId(2L)
                .iban("AZ90 QRYR Z4W2 KBDS 4RD0 9Z9B SFFA")
                .balance(new BigDecimal("700.00"))
                .accountType(AccountType.DEBIT)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        Transaction transaction = new Transaction();

        when(client.getAccountByIban("AZ90 QRYR Z4W2 KBDS 4RD0 9Z9B SFFA")).thenReturn(fromAccount);

        when(client.getAccountByIban("AZ90 QRYR Z4W2 KBDS 4RD0 9Z9B SFFA")).thenReturn(toAccount);

        when(mapper.toEntity(request)).thenReturn(transaction);

        when(repository.save(transaction)).thenReturn(transaction);

        InvalidTransactionException exception = assertThrows(InvalidTransactionException.class,
                () -> service.transfer(request));

        assertEquals(TransactionStatus.FAILED, transaction.getTransactionStatus());
        assertEquals("Source and destination must be different", exception.getMessage());
    }

    @Test
    void testTransfer_shouldThrowException_whenAmountIsZero() {

        TransferRequest request = TransferRequest.builder()
                .fromIban("AZ90 BHFE J8G3 LOCV 3EQS 4M1A CVFD")
                .toIban("AZ90 QRYR Z4W2 KBDS 4RD0 9Z9B SFFA")
                .amount(BigDecimal.ZERO)
                .description("transfer description")
                .build();

        AccountResponse fromAccount = AccountResponse.builder()
                .id(1L)
                .userId(1L)
                .iban("AZ90 BHFE J8G3 LOCV 3EQS 4M1A CVFD")
                .balance(new BigDecimal("500.00"))
                .accountType(AccountType.DEBIT)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        AccountResponse toAccount = AccountResponse.builder()
                .id(2L)
                .userId(2L)
                .iban("AZ90 QRYR Z4W2 KBDS 4RD0 9Z9B SFFA")
                .balance(new BigDecimal("700.00"))
                .accountType(AccountType.DEBIT)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        Transaction transaction = new Transaction();

        when(client.getAccountByIban("AZ90 BHFE J8G3 LOCV 3EQS 4M1A CVFD")).thenReturn(fromAccount);

        when(client.getAccountByIban("AZ90 QRYR Z4W2 KBDS 4RD0 9Z9B SFFA")).thenReturn(toAccount);

        when(mapper.toEntity(request)).thenReturn(transaction);

        when(repository.save(transaction)).thenReturn(transaction);

        InvalidAmountException exception = assertThrows(InvalidAmountException.class,
                () -> service.transfer(request));

        assertEquals(TransactionStatus.FAILED, transaction.getTransactionStatus());
        assertEquals("Amount must be greater than zero", exception.getMessage());
    }

    @Test
    void testTransfer_shouldThrowException_whenAmountIsNull() {

        TransferRequest request = TransferRequest.builder()
                .fromIban("AZ90 BHFE J8G3 LOCV 3EQS 4M1A CVFD")
                .toIban("AZ90 QRYR Z4W2 KBDS 4RD0 9Z9B SFFA")
                .amount(null)
                .description("transfer description")
                .build();

        AccountResponse fromAccount = AccountResponse.builder()
                .id(1L)
                .userId(1L)
                .iban("AZ90 BHFE J8G3 LOCV 3EQS 4M1A CVFD")
                .balance(new BigDecimal("500.00"))
                .accountType(AccountType.DEBIT)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        AccountResponse toAccount = AccountResponse.builder()
                .id(2L)
                .userId(2L)
                .iban("AZ90 QRYR Z4W2 KBDS 4RD0 9Z9B SFFA")
                .balance(new BigDecimal("700.00"))
                .accountType(AccountType.DEBIT)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        Transaction transaction = new Transaction();

        when(client.getAccountByIban("AZ90 BHFE J8G3 LOCV 3EQS 4M1A CVFD")).thenReturn(fromAccount);

        when(client.getAccountByIban("AZ90 QRYR Z4W2 KBDS 4RD0 9Z9B SFFA")).thenReturn(toAccount);

        when(mapper.toEntity(request)).thenReturn(transaction);

        when(repository.save(transaction)).thenReturn(transaction);

        InvalidAmountException exception = assertThrows(InvalidAmountException.class,
                () -> service.transfer(request));

        assertEquals(TransactionStatus.FAILED, transaction.getTransactionStatus());
        assertEquals("Amount must be greater than zero", exception.getMessage());
    }

    @Test
    void testTransfer_shouldThrowException_whenBalanceIsInsufficient() {

        TransferRequest request = TransferRequest.builder()
                .fromIban("AZ90 BHFE J8G3 LOCV 3EQS 4M1A CVFD")
                .toIban("AZ90 QRYR Z4W2 KBDS 4RD0 9Z9B SFFA")
                .amount(new BigDecimal("600.00"))
                .description("transfer description")
                .build();

        AccountResponse fromAccount = AccountResponse.builder()
                .id(1L)
                .userId(1L)
                .iban("AZ90 BHFE J8G3 LOCV 3EQS 4M1A CVFD")
                .balance(new BigDecimal("500.00"))
                .accountType(AccountType.DEBIT)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        AccountResponse toAccount = AccountResponse.builder()
                .id(2L)
                .userId(2L)
                .iban("AZ90 QRYR Z4W2 KBDS 4RD0 9Z9B SFFA")
                .balance(new BigDecimal("700.00"))
                .accountType(AccountType.DEBIT)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        Transaction transaction = new Transaction();

        when(client.getAccountByIban("AZ90 BHFE J8G3 LOCV 3EQS 4M1A CVFD")).thenReturn(fromAccount);

        when(client.getAccountByIban("AZ90 QRYR Z4W2 KBDS 4RD0 9Z9B SFFA")).thenReturn(toAccount);

        when(mapper.toEntity(request)).thenReturn(transaction);

        when(repository.save(transaction)).thenReturn(transaction);

        InsufficientBalanceException exception = assertThrows(InsufficientBalanceException.class,
                () -> service.transfer(request));

        assertEquals(TransactionStatus.FAILED, transaction.getTransactionStatus());
        assertEquals("Insufficient balance", exception.getMessage());
    }

    @Test
    void testDeposit() {

        DepositRequest request = DepositRequest.builder()
                .toIban("AZ90 QRYR Z4W2 KBDS 4RD0 9Z9B SFFA")
                .amount(new BigDecimal("100.00"))
                .build();

        AccountResponse toAccount = AccountResponse.builder()
                .id(2L)
                .userId(2L)
                .iban("AZ90 QRYR Z4W2 KBDS 4RD0 9Z9B SFFA")
                .balance(new BigDecimal("700.00"))
                .accountType(AccountType.DEBIT)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        Transaction transaction = new Transaction();
        Transaction savedTransaction = new Transaction();

        TransactionResponse response = new TransactionResponse();

        when(client.getAccountByIban("AZ90 QRYR Z4W2 KBDS 4RD0 9Z9B SFFA")).thenReturn(toAccount);

        when(mapper.toEntity(request)).thenReturn(transaction);

        when(repository.save(transaction)).thenReturn(savedTransaction);

        when(mapper.toResponse(savedTransaction)).thenReturn(response);

        TransactionResponse result = service.deposit(request);

        assertEquals(response, result);
        assertEquals(TransactionStatus.COMPLETED, transaction.getTransactionStatus());
    }

    @Test
    void testDeposit_shouldThrowException_whenAccountNotActive() {

        DepositRequest request = DepositRequest.builder()
                .toIban("AZ90 QRYR Z4W2 KBDS 4RD0 9Z9B SFFA")
                .amount(new BigDecimal("100.00"))
                .build();

        AccountResponse toAccount = AccountResponse.builder()
                .id(2L)
                .userId(2L)
                .iban("AZ90 QRYR Z4W2 KBDS 4RD0 9Z9B SFFA")
                .balance(new BigDecimal("700.00"))
                .accountType(AccountType.DEBIT)
                .accountStatus(AccountStatus.BLOCKED)
                .build();

        Transaction transaction = new Transaction();

        when(client.getAccountByIban("AZ90 QRYR Z4W2 KBDS 4RD0 9Z9B SFFA")).thenReturn(toAccount);

        when(mapper.toEntity(request)).thenReturn(transaction);

        when(repository.save(transaction)).thenReturn(transaction);

        InactiveAccountException exception = assertThrows(InactiveAccountException.class,
                () -> service.deposit(request));

        assertEquals(TransactionStatus.FAILED, transaction.getTransactionStatus());
        assertEquals("This account is not active", exception.getMessage());
    }

    @Test
    void testDeposit_shouldThrowException_whenAmountIsNull() {

        DepositRequest request = DepositRequest.builder()
                .toIban("AZ90 QRYR Z4W2 KBDS 4RD0 9Z9B SFFA")
                .amount(null)
                .build();

        AccountResponse toAccount = AccountResponse.builder()
                .id(2L)
                .userId(2L)
                .iban("AZ90 QRYR Z4W2 KBDS 4RD0 9Z9B SFFA")
                .balance(new BigDecimal("700.00"))
                .accountType(AccountType.DEBIT)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        Transaction transaction = new Transaction();

        when(client.getAccountByIban("AZ90 QRYR Z4W2 KBDS 4RD0 9Z9B SFFA")).thenReturn(toAccount);

        when(mapper.toEntity(request)).thenReturn(transaction);

        when(repository.save(transaction)).thenReturn(transaction);

        InvalidAmountException exception = assertThrows(InvalidAmountException.class,
                () -> service.deposit(request));

        assertEquals(TransactionStatus.FAILED, transaction.getTransactionStatus());
        assertEquals("Amount must be greater than zero", exception.getMessage());
    }

    @Test
    void testDeposit_shouldThrowException_whenAmountIsZero() {

        DepositRequest request = DepositRequest.builder()
                .toIban("AZ90 QRYR Z4W2 KBDS 4RD0 9Z9B SFFA")
                .amount(BigDecimal.ZERO)
                .build();

        AccountResponse toAccount = AccountResponse.builder()
                .id(2L)
                .userId(2L)
                .iban("AZ90 QRYR Z4W2 KBDS 4RD0 9Z9B SFFA")
                .balance(new BigDecimal("700.00"))
                .accountType(AccountType.DEBIT)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        Transaction transaction = new Transaction();

        when(client.getAccountByIban("AZ90 QRYR Z4W2 KBDS 4RD0 9Z9B SFFA")).thenReturn(toAccount);

        when(mapper.toEntity(request)).thenReturn(transaction);

        when(repository.save(transaction)).thenReturn(transaction);

        InvalidAmountException exception = assertThrows(InvalidAmountException.class,
                () -> service.deposit(request));

        assertEquals(TransactionStatus.FAILED, transaction.getTransactionStatus());
        assertEquals("Amount must be greater than zero", exception.getMessage());
    }

    @Test
    void testWithdrawal() {

        WithdrawalRequest request = WithdrawalRequest.builder()
                .fromIban("AZ90 BHFE J8G3 LOCV 3EQS 4M1A CVFD")
                .amount(new BigDecimal("100.00"))
                .build();

        AccountResponse fromAccount = AccountResponse.builder()
                .id(1L)
                .userId(1L)
                .iban("AZ90 BHFE J8G3 LOCV 3EQS 4M1A CVFD")
                .balance(new BigDecimal("500.00"))
                .accountType(AccountType.DEBIT)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        Transaction transaction = new Transaction();
        Transaction savedTransaction = new Transaction();
        TransactionResponse response = new TransactionResponse();

        when(client.getAccountByIban("AZ90 BHFE J8G3 LOCV 3EQS 4M1A CVFD")).thenReturn(fromAccount);

        when(mapper.toEntity(request)).thenReturn(transaction);

        when(repository.save(transaction)).thenReturn(savedTransaction);

        when(mapper.toResponse(savedTransaction)).thenReturn(response);

        TransactionResponse result = service.withdrawal(request);

        assertEquals(response, result);
    }

    @Test
    void testWithdrawal_shouldThrowException_whenAccountNotActive() {

        WithdrawalRequest request = WithdrawalRequest.builder()
                .fromIban("AZ90 BHFE J8G3 LOCV 3EQS 4M1A CVFD")
                .amount(new BigDecimal("100.00"))
                .build();

        AccountResponse fromAccount = AccountResponse.builder()
                .id(1L)
                .userId(1L)
                .iban("AZ90 BHFE J8G3 LOCV 3EQS 4M1A CVFD")
                .balance(new BigDecimal("500.00"))
                .accountType(AccountType.DEBIT)
                .accountStatus(AccountStatus.BLOCKED)
                .build();

        Transaction transaction = new Transaction();

        when(client.getAccountByIban("AZ90 BHFE J8G3 LOCV 3EQS 4M1A CVFD")).thenReturn(fromAccount);

        when(mapper.toEntity(request)).thenReturn(transaction);

        when(repository.save(transaction)).thenReturn(transaction);

        InactiveAccountException exception = assertThrows(InactiveAccountException.class,
                () -> service.withdrawal(request));

        assertEquals(TransactionStatus.FAILED, transaction.getTransactionStatus());
        assertEquals("This account is not active", exception.getMessage());
    }

    @Test
    void testWithdrawal_shouldThrowException_whenAmountIsNull() {

        WithdrawalRequest request = WithdrawalRequest.builder()
                .fromIban("AZ90 BHFE J8G3 LOCV 3EQS 4M1A CVFD")
                .amount(null)
                .build();

        AccountResponse fromAccount = AccountResponse.builder()
                .id(1L)
                .userId(1L)
                .iban("AZ90 BHFE J8G3 LOCV 3EQS 4M1A CVFD")
                .balance(new BigDecimal("500.00"))
                .accountType(AccountType.DEBIT)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        Transaction transaction = new Transaction();

        when(client.getAccountByIban("AZ90 BHFE J8G3 LOCV 3EQS 4M1A CVFD")).thenReturn(fromAccount);

        when(mapper.toEntity(request)).thenReturn(transaction);

        when(repository.save(transaction)).thenReturn(transaction);

        InvalidAmountException exception = assertThrows(InvalidAmountException.class,
                () -> service.withdrawal(request));

        assertEquals(TransactionStatus.FAILED, transaction.getTransactionStatus());
        assertEquals("Amount must be greater than zero", exception.getMessage());
    }

    @Test
    void testWithdrawal_shouldThrowException_whenAmountIsZero() {

        WithdrawalRequest request = WithdrawalRequest.builder()
                .fromIban("AZ90 BHFE J8G3 LOCV 3EQS 4M1A CVFD")
                .amount(BigDecimal.ZERO)
                .build();

        AccountResponse fromAccount = AccountResponse.builder()
                .id(1L)
                .userId(1L)
                .iban("AZ90 BHFE J8G3 LOCV 3EQS 4M1A CVFD")
                .balance(new BigDecimal("500.00"))
                .accountType(AccountType.DEBIT)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        Transaction transaction = new Transaction();

        when(client.getAccountByIban("AZ90 BHFE J8G3 LOCV 3EQS 4M1A CVFD")).thenReturn(fromAccount);

        when(mapper.toEntity(request)).thenReturn(transaction);

        when(repository.save(transaction)).thenReturn(transaction);

        InvalidAmountException exception = assertThrows(InvalidAmountException.class,
                () -> service.withdrawal(request));

        assertEquals(TransactionStatus.FAILED, transaction.getTransactionStatus());
        assertEquals("Amount must be greater than zero", exception.getMessage());
    }

    @Test
    void testWithdrawal_shouldThrowException_whenBalanceIsInsufficient() {

        WithdrawalRequest request = WithdrawalRequest.builder()
                .fromIban("AZ90 BHFE J8G3 LOCV 3EQS 4M1A CVFD")
                .amount(new BigDecimal("800.00"))
                .build();

        AccountResponse fromAccount = AccountResponse.builder()
                .id(1L)
                .userId(1L)
                .iban("AZ90 BHFE J8G3 LOCV 3EQS 4M1A CVFD")
                .balance(new BigDecimal("500.00"))
                .accountType(AccountType.DEBIT)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        Transaction transaction = new Transaction();

        when(client.getAccountByIban("AZ90 BHFE J8G3 LOCV 3EQS 4M1A CVFD")).thenReturn(fromAccount);

        when(mapper.toEntity(request)).thenReturn(transaction);

        when(repository.save(transaction)).thenReturn(transaction);

        InsufficientBalanceException exception = assertThrows(InsufficientBalanceException.class,
                () -> service.withdrawal(request));

        assertEquals(TransactionStatus.FAILED, transaction.getTransactionStatus());
        assertEquals("Insufficient balance", exception.getMessage());
    }

    @Test
    void testGetTransactionById() {
        Long transactionId = 1L;

        Transaction transaction = new Transaction();
        transaction.setId(transactionId);

        TransactionResponse response = new TransactionResponse();

        when(repository.findById(transactionId)).thenReturn(Optional.of(transaction));

        when(mapper.toResponse(transaction)).thenReturn(response);

        TransactionResponse result = service.getTransactionById(transactionId);

        assertEquals(response, result);
    }

    @Test
    void testGetTransactionById_shouldThrowException_whenTransactionNotFound() {
        Long transactionId = 1L;

        when(repository.findById(transactionId)).thenReturn(Optional.empty());

        TransactionNotFoundException exception = assertThrows(TransactionNotFoundException.class,
                () -> service.getTransactionById(transactionId));

        assertEquals("Transaction not found", exception.getMessage());
    }

    @Test
    void testGetAllTransactions() {
        Transaction transaction1 = new Transaction();
        Transaction transaction2 = new Transaction();

        TransactionResponse response1 = new TransactionResponse();
        TransactionResponse response2 = new TransactionResponse();

        when(repository.findAll()).thenReturn(List.of(transaction1, transaction2));

        when(mapper.toResponse(transaction1)).thenReturn(response1);

        when(mapper.toResponse(transaction2)).thenReturn(response2);

        List<TransactionResponse> result = service.getAllTransactions();

        assertEquals(2, result.size());

        assertEquals(response1, result.get(0));
        assertEquals(response2, result.get(1));
    }

    @Test
    void testGetAllTransactionsByIban() {
        String iban = "AZ90 BHFE J8G3 LOCV 3EQS 4M1A CVFD";

        Transaction transaction1 = new Transaction();
        Transaction transaction2 = new Transaction();

        TransactionResponse response1 = new TransactionResponse();
        TransactionResponse response2 = new TransactionResponse();

        when(repository.findByFromIbanOrToIban(iban, iban)).thenReturn(List.of(transaction1, transaction2));

        when(mapper.toResponse(transaction1)).thenReturn(response1);

        when(mapper.toResponse(transaction2)).thenReturn(response2);

        List<TransactionResponse> result = service.getTransactionsByIban(iban);

        assertEquals(2, result.size());

        assertEquals(response1, result.get(0));
        assertEquals(response2, result.get(1));
    }

}