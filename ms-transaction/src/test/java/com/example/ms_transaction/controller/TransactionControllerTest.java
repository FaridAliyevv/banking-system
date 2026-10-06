package com.example.ms_transaction.controller;

import com.example.ms_transaction.dto.request.DepositRequest;
import com.example.ms_transaction.dto.request.TransferRequest;
import com.example.ms_transaction.dto.request.WithdrawalRequest;
import com.example.ms_transaction.dto.response.TransactionResponse;
import com.example.ms_transaction.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testTransfer() throws Exception {

        TransferRequest request = TransferRequest.builder()
                .fromIban("AZ90 BHFE J8G3 LOCV 3EQS 4M1A CVFD")
                .toIban("AZ90 QRYR Z4W2 KBDS 4RD0 9Z9B SFFA")
                .amount(new BigDecimal("100.00"))
                .description("transfer description")
                .build();

        TransactionResponse response = new TransactionResponse();

        when(service.transfer(request)).thenReturn(response);

        mockMvc.perform(post("/transactions/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testDeposit() throws Exception {

        DepositRequest request = DepositRequest.builder()
                .toIban("AZ90 QRYR Z4W2 KBDS 4RD0 9Z9B SFFA")
                .amount(new BigDecimal("100.00"))
                .description("deposit description")
                .build();

        TransactionResponse response = new TransactionResponse();

        when(service.deposit(request)).thenReturn(response);

        mockMvc.perform(post("/transactions/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testWithdrawal() throws Exception {

        WithdrawalRequest request = WithdrawalRequest.builder()
                .fromIban("AZ90 BHFE J8G3 LOCV 3EQS 4M1A CVFD")
                .amount(new BigDecimal("100.00"))
                .description("withdrawal description")
                .build();

        TransactionResponse response = new TransactionResponse();

        when(service.withdrawal(request)).thenReturn(response);

        mockMvc.perform(post("/transactions/withdrawal")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testGetTransactionById() throws Exception {
        Long transactionId = 1L;

        TransactionResponse response = new TransactionResponse();

        when(service.getTransactionById(transactionId)).thenReturn(response);

        mockMvc.perform(get("/transactions/{id}", transactionId))
                .andExpect(status().isOk());
    }

    @Test
    void testGetTransactionsByIban() throws Exception {
        String iban = "AZ90 BHFE J8G3 LOCV 3EQS 4M1A CVFD";

        TransactionResponse response1 = new TransactionResponse();
        TransactionResponse response2 = new TransactionResponse();

        when(service.getTransactionsByIban(iban)).thenReturn(List.of(response1, response2));

        mockMvc.perform(get("/transactions/account/{iban}", iban))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAllTransactions() throws Exception {
        TransactionResponse response1 = new TransactionResponse();
        TransactionResponse response2 = new TransactionResponse();

        when(service.getAllTransactions()).thenReturn(List.of(response1, response2));

        mockMvc.perform(get("/transactions"))
                .andExpect(status().isOk());
    }
}