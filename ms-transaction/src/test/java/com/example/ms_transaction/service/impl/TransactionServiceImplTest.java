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
import com.example.ms_transaction.security.JwtService;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.example.ms_transaction.util.TestConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository repository;

    @Mock
    private TransactionMapper mapper;

    @Mock
    private AccountClient client;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private TransactionServiceImpl service;

    @Test
    void testTransfer() {

        TransferRequest request = TransferRequest.builder()
                .fromIban(FROM_IBAN)
                .toIban(TO_IBAN)
                .amount(new BigDecimal("100.00"))
                .description(TRANSFER_DESCRIPTION)
                .build();

        AccountResponse fromAccount = AccountResponse.builder()
                .id(ID)
                .userId(USER_ID)
                .iban(FROM_IBAN)
                .balance(new BigDecimal("500.00"))
                .accountType(AccountType.DEBIT)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        AccountResponse toAccount = AccountResponse.builder()
                .id(ID2)
                .userId(USER_ID2)
                .iban(TO_IBAN)
                .balance(new BigDecimal("700.00"))
                .accountType(AccountType.DEBIT)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        Transaction transaction = new Transaction();
        Transaction savedTransaction = new Transaction();

        TransactionResponse response = new TransactionResponse();

        when(client.getAccountByIban(FROM_IBAN, TOKEN)).thenReturn(fromAccount);

        when(client.getAccountByIban(TO_IBAN, TOKEN)).thenReturn(toAccount);

        when(mapper.toEntity(request)).thenReturn(transaction);

        when(repository.save(transaction)).thenReturn(savedTransaction);

        when(mapper.toResponse(savedTransaction)).thenReturn(response);

        TransactionResponse result = service.transfer(request, TOKEN);

        assertEquals(response, result);
        assertEquals(TransactionType.TRANSFER, transaction.getTransactionType());
        assertEquals(TransactionStatus.COMPLETED, transaction.getTransactionStatus());
    }

    @Test
    void testTransfer_shouldThrowException_whenSourceAccountNotActive() {

        TransferRequest request = TransferRequest.builder()
                .fromIban(FROM_IBAN)
                .toIban(TO_IBAN)
                .amount(new BigDecimal("100.00"))
                .description(TRANSFER_DESCRIPTION)
                .build();

        AccountResponse fromAccount = AccountResponse.builder()
                .id(ID)
                .userId(USER_ID)
                .iban(FROM_IBAN)
                .balance(new BigDecimal("500.00"))
                .accountType(AccountType.DEBIT)
                .accountStatus(AccountStatus.BLOCKED)
                .build();

        AccountResponse toAccount = AccountResponse.builder()
                .id(ID2)
                .userId(USER_ID2)
                .iban(TO_IBAN)
                .balance(new BigDecimal("700.00"))
                .accountType(AccountType.DEBIT)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        Transaction transaction = new Transaction();

        when(client.getAccountByIban(FROM_IBAN, TOKEN)).thenReturn(fromAccount);

        when(client.getAccountByIban(TO_IBAN, TOKEN)).thenReturn(toAccount);

        when(mapper.toEntity(request)).thenReturn(transaction);

        when(repository.save(transaction)).thenReturn(transaction);

        InactiveAccountException exception = assertThrows(InactiveAccountException.class,
                () -> service.transfer(request, TOKEN));

        assertEquals(TransactionStatus.FAILED, transaction.getTransactionStatus());
        assertEquals("Source account is not active", exception.getMessage());
    }

    @Test
    void testTransfer_shouldThrowException_whenDestinationAccountNotActive() {

        TransferRequest request = TransferRequest.builder()
                .fromIban(FROM_IBAN)
                .toIban(TO_IBAN)
                .amount(new BigDecimal("100.00"))
                .description(TRANSFER_DESCRIPTION)
                .build();

        AccountResponse fromAccount = AccountResponse.builder()
                .id(ID)
                .userId(USER_ID)
                .iban(FROM_IBAN)
                .balance(new BigDecimal("500.00"))
                .accountType(AccountType.DEBIT)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        AccountResponse toAccount = AccountResponse.builder()
                .id(ID2)
                .userId(USER_ID2)
                .iban(TO_IBAN)
                .balance(new BigDecimal("700.00"))
                .accountType(AccountType.DEBIT)
                .accountStatus(AccountStatus.BLOCKED)
                .build();

        Transaction transaction = new Transaction();

        when(client.getAccountByIban(FROM_IBAN, TOKEN)).thenReturn(fromAccount);

        when(client.getAccountByIban(TO_IBAN, TOKEN)).thenReturn(toAccount);

        when(mapper.toEntity(request)).thenReturn(transaction);

        when(repository.save(transaction)).thenReturn(transaction);

        InactiveAccountException exception = assertThrows(InactiveAccountException.class,
                () -> service.transfer(request, TOKEN));

        assertEquals(TransactionStatus.FAILED, transaction.getTransactionStatus());
        assertEquals("Destination account is not active", exception.getMessage());
    }

    @Test
    void testTransfer_shouldThrowException_whenSourceAndDestinationAreEqual() {

        TransferRequest request = TransferRequest.builder()
                .fromIban(TO_IBAN)
                .toIban(TO_IBAN)
                .amount(new BigDecimal("100.00"))
                .description(TRANSFER_DESCRIPTION)
                .build();

        AccountResponse fromAccount = AccountResponse.builder()
                .id(ID)
                .userId(USER_ID)
                .iban(TO_IBAN)
                .balance(new BigDecimal("500.00"))
                .accountType(AccountType.DEBIT)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        AccountResponse toAccount = AccountResponse.builder()
                .id(ID2)
                .userId(USER_ID2)
                .iban(TO_IBAN)
                .balance(new BigDecimal("700.00"))
                .accountType(AccountType.DEBIT)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        Transaction transaction = new Transaction();

        when(client.getAccountByIban(TO_IBAN, TOKEN)).thenReturn(fromAccount);

        when(client.getAccountByIban(TO_IBAN, TOKEN)).thenReturn(toAccount);

        when(mapper.toEntity(request)).thenReturn(transaction);

        when(repository.save(transaction)).thenReturn(transaction);

        InvalidTransactionException exception = assertThrows(InvalidTransactionException.class,
                () -> service.transfer(request, TOKEN));

        assertEquals(TransactionStatus.FAILED, transaction.getTransactionStatus());
        assertEquals("Source and destination must be different", exception.getMessage());
    }

    @Test
    void testTransfer_shouldThrowException_whenAmountIsZero() {

        TransferRequest request = TransferRequest.builder()
                .fromIban(FROM_IBAN)
                .toIban(TO_IBAN)
                .amount(BigDecimal.ZERO)
                .description(TRANSFER_DESCRIPTION)
                .build();

        AccountResponse fromAccount = AccountResponse.builder()
                .id(ID)
                .userId(USER_ID)
                .iban(FROM_IBAN)
                .balance(new BigDecimal("500.00"))
                .accountType(AccountType.DEBIT)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        AccountResponse toAccount = AccountResponse.builder()
                .id(ID2)
                .userId(USER_ID2)
                .iban(TO_IBAN)
                .balance(new BigDecimal("700.00"))
                .accountType(AccountType.DEBIT)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        Transaction transaction = new Transaction();

        when(client.getAccountByIban(FROM_IBAN, TOKEN)).thenReturn(fromAccount);

        when(client.getAccountByIban(TO_IBAN, TOKEN)).thenReturn(toAccount);

        when(mapper.toEntity(request)).thenReturn(transaction);

        when(repository.save(transaction)).thenReturn(transaction);

        InvalidAmountException exception = assertThrows(InvalidAmountException.class,
                () -> service.transfer(request, TOKEN));

        assertEquals(TransactionStatus.FAILED, transaction.getTransactionStatus());
        assertEquals("Amount must be greater than zero", exception.getMessage());
    }

    @Test
    void testTransfer_shouldThrowException_whenAmountIsNull() {

        TransferRequest request = TransferRequest.builder()
                .fromIban(FROM_IBAN)
                .toIban(TO_IBAN)
                .amount(null)
                .description(TRANSFER_DESCRIPTION)
                .build();

        AccountResponse fromAccount = AccountResponse.builder()
                .id(ID)
                .userId(USER_ID)
                .iban(FROM_IBAN)
                .balance(new BigDecimal("500.00"))
                .accountType(AccountType.DEBIT)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        AccountResponse toAccount = AccountResponse.builder()
                .id(ID2)
                .userId(USER_ID2)
                .iban(TO_IBAN)
                .balance(new BigDecimal("700.00"))
                .accountType(AccountType.DEBIT)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        Transaction transaction = new Transaction();

        when(client.getAccountByIban(FROM_IBAN, TOKEN)).thenReturn(fromAccount);

        when(client.getAccountByIban(TO_IBAN, TOKEN)).thenReturn(toAccount);

        when(mapper.toEntity(request)).thenReturn(transaction);

        when(repository.save(transaction)).thenReturn(transaction);

        InvalidAmountException exception = assertThrows(InvalidAmountException.class,
                () -> service.transfer(request, TOKEN));

        assertEquals(TransactionStatus.FAILED, transaction.getTransactionStatus());
        assertEquals("Amount must be greater than zero", exception.getMessage());
    }

    @Test
    void testTransfer_shouldThrowException_whenBalanceIsInsufficient() {

        TransferRequest request = TransferRequest.builder()
                .fromIban(FROM_IBAN)
                .toIban(TO_IBAN)
                .amount(new BigDecimal("600.00"))
                .description(TRANSFER_DESCRIPTION)
                .build();

        AccountResponse fromAccount = AccountResponse.builder()
                .id(ID)
                .userId(USER_ID)
                .iban(FROM_IBAN)
                .balance(new BigDecimal("500.00"))
                .accountType(AccountType.DEBIT)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        AccountResponse toAccount = AccountResponse.builder()
                .id(ID2)
                .userId(USER_ID2)
                .iban(TO_IBAN)
                .balance(new BigDecimal("700.00"))
                .accountType(AccountType.DEBIT)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        Transaction transaction = new Transaction();

        when(client.getAccountByIban(FROM_IBAN, TOKEN)).thenReturn(fromAccount);

        when(client.getAccountByIban(TO_IBAN, TOKEN)).thenReturn(toAccount);

        when(mapper.toEntity(request)).thenReturn(transaction);

        when(repository.save(transaction)).thenReturn(transaction);

        InsufficientBalanceException exception = assertThrows(InsufficientBalanceException.class,
                () -> service.transfer(request, TOKEN));

        assertEquals(TransactionStatus.FAILED, transaction.getTransactionStatus());
        assertEquals("Insufficient balance", exception.getMessage());
    }

    @Test
    void testDeposit() {

        DepositRequest request = DepositRequest.builder()
                .toIban(TO_IBAN)
                .amount(new BigDecimal("100.00"))
                .build();

        AccountResponse toAccount = AccountResponse.builder()
                .id(ID2)
                .userId(USER_ID2)
                .iban(TO_IBAN)
                .balance(new BigDecimal("700.00"))
                .accountType(AccountType.DEBIT)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        Transaction transaction = new Transaction();
        Transaction savedTransaction = new Transaction();

        TransactionResponse response = new TransactionResponse();

        when(client.getAccountByIban(TO_IBAN, TOKEN)).thenReturn(toAccount);

        when(mapper.toEntity(request)).thenReturn(transaction);

        when(repository.save(transaction)).thenReturn(savedTransaction);

        when(mapper.toResponse(savedTransaction)).thenReturn(response);

        TransactionResponse result = service.deposit(request, TOKEN);

        assertEquals(response, result);
        assertEquals(TransactionStatus.COMPLETED, transaction.getTransactionStatus());
    }

    @Test
    void testDeposit_shouldThrowException_whenAccountNotActive() {

        DepositRequest request = DepositRequest.builder()
                .toIban(TO_IBAN)
                .amount(new BigDecimal("100.00"))
                .build();

        AccountResponse toAccount = AccountResponse.builder()
                .id(ID2)
                .userId(USER_ID2)
                .iban(TO_IBAN)
                .balance(new BigDecimal("700.00"))
                .accountType(AccountType.DEBIT)
                .accountStatus(AccountStatus.BLOCKED)
                .build();

        Transaction transaction = new Transaction();

        when(client.getAccountByIban(TO_IBAN, TOKEN)).thenReturn(toAccount);

        when(mapper.toEntity(request)).thenReturn(transaction);

        when(repository.save(transaction)).thenReturn(transaction);

        InactiveAccountException exception = assertThrows(InactiveAccountException.class,
                () -> service.deposit(request, TOKEN));

        assertEquals(TransactionStatus.FAILED, transaction.getTransactionStatus());
        assertEquals("This account is not active", exception.getMessage());
    }

    @Test
    void testDeposit_shouldThrowException_whenAmountIsNull() {

        DepositRequest request = DepositRequest.builder()
                .toIban(TO_IBAN)
                .amount(null)
                .build();

        AccountResponse toAccount = AccountResponse.builder()
                .id(ID2)
                .userId(USER_ID2)
                .iban(TO_IBAN)
                .balance(new BigDecimal("700.00"))
                .accountType(AccountType.DEBIT)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        Transaction transaction = new Transaction();

        when(client.getAccountByIban(TO_IBAN, TOKEN)).thenReturn(toAccount);

        when(mapper.toEntity(request)).thenReturn(transaction);

        when(repository.save(transaction)).thenReturn(transaction);

        InvalidAmountException exception = assertThrows(InvalidAmountException.class,
                () -> service.deposit(request, TOKEN));

        assertEquals(TransactionStatus.FAILED, transaction.getTransactionStatus());
        assertEquals("Amount must be greater than zero", exception.getMessage());
    }

    @Test
    void testDeposit_shouldThrowException_whenAmountIsZero() {

        DepositRequest request = DepositRequest.builder()
                .toIban(TO_IBAN)
                .amount(BigDecimal.ZERO)
                .build();

        AccountResponse toAccount = AccountResponse.builder()
                .id(ID2)
                .userId(USER_ID2)
                .iban(TO_IBAN)
                .balance(new BigDecimal("700.00"))
                .accountType(AccountType.DEBIT)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        Transaction transaction = new Transaction();

        when(client.getAccountByIban(TO_IBAN, TOKEN)).thenReturn(toAccount);

        when(mapper.toEntity(request)).thenReturn(transaction);

        when(repository.save(transaction)).thenReturn(transaction);

        InvalidAmountException exception = assertThrows(InvalidAmountException.class,
                () -> service.deposit(request, TOKEN));

        assertEquals(TransactionStatus.FAILED, transaction.getTransactionStatus());
        assertEquals("Amount must be greater than zero", exception.getMessage());
    }

    @Test
    void testWithdrawal() {

        WithdrawalRequest request = WithdrawalRequest.builder()
                .fromIban(FROM_IBAN)
                .amount(new BigDecimal("100.00"))
                .build();

        AccountResponse fromAccount = AccountResponse.builder()
                .id(ID)
                .userId(USER_ID)
                .iban(FROM_IBAN)
                .balance(new BigDecimal("500.00"))
                .accountType(AccountType.DEBIT)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        Transaction transaction = new Transaction();
        Transaction savedTransaction = new Transaction();
        TransactionResponse response = new TransactionResponse();

        when(client.getAccountByIban(FROM_IBAN, TOKEN)).thenReturn(fromAccount);

        when(mapper.toEntity(request)).thenReturn(transaction);

        when(repository.save(transaction)).thenReturn(savedTransaction);

        when(mapper.toResponse(savedTransaction)).thenReturn(response);

        TransactionResponse result = service.withdrawal(request, TOKEN);

        assertEquals(response, result);
    }

    @Test
    void testWithdrawal_shouldThrowException_whenAccountNotActive() {

        WithdrawalRequest request = WithdrawalRequest.builder()
                .fromIban(FROM_IBAN)
                .amount(new BigDecimal("100.00"))
                .build();

        AccountResponse fromAccount = AccountResponse.builder()
                .id(ID)
                .userId(USER_ID)
                .iban(FROM_IBAN)
                .balance(new BigDecimal("500.00"))
                .accountType(AccountType.DEBIT)
                .accountStatus(AccountStatus.BLOCKED)
                .build();

        Transaction transaction = new Transaction();

        when(client.getAccountByIban(FROM_IBAN, TOKEN)).thenReturn(fromAccount);

        when(mapper.toEntity(request)).thenReturn(transaction);

        when(repository.save(transaction)).thenReturn(transaction);

        InactiveAccountException exception = assertThrows(InactiveAccountException.class,
                () -> service.withdrawal(request, TOKEN));

        assertEquals(TransactionStatus.FAILED, transaction.getTransactionStatus());
        assertEquals("This account is not active", exception.getMessage());
    }

    @Test
    void testWithdrawal_shouldThrowException_whenAmountIsNull() {

        WithdrawalRequest request = WithdrawalRequest.builder()
                .fromIban(FROM_IBAN)
                .amount(null)
                .build();

        AccountResponse fromAccount = AccountResponse.builder()
                .id(ID)
                .userId(USER_ID)
                .iban(FROM_IBAN)
                .balance(new BigDecimal("500.00"))
                .accountType(AccountType.DEBIT)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        Transaction transaction = new Transaction();

        when(client.getAccountByIban(FROM_IBAN, TOKEN)).thenReturn(fromAccount);

        when(mapper.toEntity(request)).thenReturn(transaction);

        when(repository.save(transaction)).thenReturn(transaction);

        InvalidAmountException exception = assertThrows(InvalidAmountException.class,
                () -> service.withdrawal(request, TOKEN));

        assertEquals(TransactionStatus.FAILED, transaction.getTransactionStatus());
        assertEquals("Amount must be greater than zero", exception.getMessage());
    }

    @Test
    void testWithdrawal_shouldThrowException_whenAmountIsZero() {

        WithdrawalRequest request = WithdrawalRequest.builder()
                .fromIban(FROM_IBAN)
                .amount(BigDecimal.ZERO)
                .build();

        AccountResponse fromAccount = AccountResponse.builder()
                .id(ID)
                .userId(USER_ID)
                .iban(FROM_IBAN)
                .balance(new BigDecimal("500.00"))
                .accountType(AccountType.DEBIT)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        Transaction transaction = new Transaction();

        when(client.getAccountByIban(FROM_IBAN, TOKEN)).thenReturn(fromAccount);

        when(mapper.toEntity(request)).thenReturn(transaction);

        when(repository.save(transaction)).thenReturn(transaction);

        InvalidAmountException exception = assertThrows(InvalidAmountException.class,
                () -> service.withdrawal(request, TOKEN));

        assertEquals(TransactionStatus.FAILED, transaction.getTransactionStatus());
        assertEquals("Amount must be greater than zero", exception.getMessage());
    }

    @Test
    void testWithdrawal_shouldThrowException_whenBalanceIsInsufficient() {

        WithdrawalRequest request = WithdrawalRequest.builder()
                .fromIban(FROM_IBAN)
                .amount(new BigDecimal("800.00"))
                .build();

        AccountResponse fromAccount = AccountResponse.builder()
                .id(ID)
                .userId(USER_ID)
                .iban(FROM_IBAN)
                .balance(new BigDecimal("500.00"))
                .accountType(AccountType.DEBIT)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        Transaction transaction = new Transaction();

        when(client.getAccountByIban(FROM_IBAN, TOKEN)).thenReturn(fromAccount);

        when(mapper.toEntity(request)).thenReturn(transaction);

        when(repository.save(transaction)).thenReturn(transaction);

        InsufficientBalanceException exception = assertThrows(InsufficientBalanceException.class,
                () -> service.withdrawal(request, TOKEN));

        assertEquals(TransactionStatus.FAILED, transaction.getTransactionStatus());
        assertEquals("Insufficient balance", exception.getMessage());
    }

    @Test
    void testGetTransactionById() {
        Transaction transaction = new Transaction();
        transaction.setId(TRANSACTION_ID);

        TransactionResponse response = new TransactionResponse();

        when(repository.findById(TRANSACTION_ID)).thenReturn(Optional.of(transaction));

        when(mapper.toResponse(transaction)).thenReturn(response);

        TransactionResponse result = service.getTransactionById(TRANSACTION_ID);

        assertEquals(response, result);
    }

    @Test
    void testGetTransactionById_shouldThrowException_whenTransactionNotFound() {
        when(repository.findById(TRANSACTION_ID)).thenReturn(Optional.empty());

        TransactionNotFoundException exception = assertThrows(TransactionNotFoundException.class,
                () -> service.getTransactionById(TRANSACTION_ID));

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
        Transaction transaction1 = new Transaction();
        Transaction transaction2 = new Transaction();

        TransactionResponse response1 = new TransactionResponse();
        TransactionResponse response2 = new TransactionResponse();

        when(repository.findByFromIbanOrToIban(IBAN, IBAN)).thenReturn(List.of(transaction1, transaction2));

        when(mapper.toResponse(transaction1)).thenReturn(response1);

        when(mapper.toResponse(transaction2)).thenReturn(response2);

        List<TransactionResponse> result = service.getTransactionsByIban(IBAN, TOKEN);

        assertEquals(2, result.size());

        assertEquals(response1, result.get(0));
        assertEquals(response2, result.get(1));
    }

    @Test
    void testGetTransactionsByIban_shouldThrowAccessDenied_whenAccountIsForbidden() {
        FeignException.Forbidden forbidden = mock(FeignException.Forbidden.class);

        when(client.getAccountByIban(IBAN, TOKEN)).thenThrow(forbidden);

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> service.getTransactionsByIban(IBAN, TOKEN));
    }

}