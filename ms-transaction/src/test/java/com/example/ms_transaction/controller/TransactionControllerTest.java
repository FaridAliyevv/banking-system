package com.example.ms_transaction.controller;

import com.example.ms_transaction.dto.request.DepositRequest;
import com.example.ms_transaction.dto.request.TransferRequest;
import com.example.ms_transaction.dto.request.WithdrawalRequest;
import com.example.ms_transaction.dto.response.TransactionResponse;
import com.example.ms_transaction.security.JwtService;
import com.example.ms_transaction.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static com.example.ms_transaction.util.TestConstants.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private TransactionService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testTransfer() throws Exception {
        TransferRequest request = TransferRequest.builder()
                .fromIban(FROM_IBAN)
                .toIban(TO_IBAN)
                .amount(new BigDecimal("100.00"))
                .description(TRANSFER_DESCRIPTION)
                .build();

        when(service.transfer(any(TransferRequest.class), eq(TOKEN)))
                .thenReturn(new TransactionResponse());

        mockMvc.perform(post("/transactions/transfer")
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testDeposit() throws Exception {
        DepositRequest request = DepositRequest.builder()
                .toIban(TO_IBAN)
                .amount(new BigDecimal("100.00"))
                .description(DEPOSIT_DESCRIPTION)
                .build();

        when(service.deposit(any(DepositRequest.class), eq(TOKEN)))
                .thenReturn(new TransactionResponse());

        mockMvc.perform(post("/transactions/deposit")
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testWithdrawal() throws Exception {
        WithdrawalRequest request = WithdrawalRequest.builder()
                .fromIban(FROM_IBAN)
                .amount(new BigDecimal("100.00"))
                .description(WITHDRAWAL_DESCRIPTION)
                .build();

        when(service.withdrawal(any(WithdrawalRequest.class), eq(TOKEN)))
                .thenReturn(new TransactionResponse());

        mockMvc.perform(post("/transactions/withdrawal")
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testGetTransactionById() throws Exception {
        when(service.getTransactionById(ID)).thenReturn(new TransactionResponse());

        mockMvc.perform(get("/transactions/{id}", ID))
                .andExpect(status().isOk());
    }

    @Test
    void testGetTransactionsByIban() throws Exception {
        when(service.getTransactionsByIban(IBAN, TOKEN))
                .thenReturn(List.of(new TransactionResponse(), new TransactionResponse()));

        mockMvc.perform(get("/transactions/account/{iban}", IBAN)
                        .header("Authorization", TOKEN))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAllTransactions() throws Exception {
        when(service.getAllTransactions())
                .thenReturn(List.of(new TransactionResponse(), new TransactionResponse()));

        mockMvc.perform(get("/transactions"))
                .andExpect(status().isOk());
    }
}