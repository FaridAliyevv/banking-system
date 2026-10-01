package com.example.ms_account.controller;

import com.example.ms_account.dto.request.CreateAccountRequest;
import com.example.ms_account.dto.request.UpdateAccountRequest;
import com.example.ms_account.dto.response.AccountResponse;
import com.example.ms_account.enums.AccountStatus;
import com.example.ms_account.enums.AccountType;
import com.example.ms_account.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateAccount() throws Exception {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setUserId(1L);
        request.setAccountType(AccountType.DEBIT);

        AccountResponse response = new AccountResponse();

        when(service.createAccount(request)).thenReturn(response);

        mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAccountById() throws Exception {
        Long accountId = 1L;

        AccountResponse response = new AccountResponse();

        when(service.getAccountById(accountId)).thenReturn(response);

        mockMvc.perform(get("/accounts/{id}", accountId))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAccountByIban() throws Exception {
        String iban = "AZ90 QRYR Z4W2 KBDS 4RD0 9Z9B SFFA";

        AccountResponse response = new AccountResponse();

        when(service.getAccountByIban(iban)).thenReturn(response);

        mockMvc.perform(get("/accounts/iban/{iban}", iban))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAllAccounts() throws Exception {
        AccountResponse response1 = new AccountResponse();
        AccountResponse response2 = new AccountResponse();

        when(service.getAllAccounts()).thenReturn(List.of(response1, response2));

        mockMvc.perform(get("/accounts"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAllAccountsByUserId() throws Exception {
        Long userId = 1L;

        AccountResponse response1 = new AccountResponse();
        AccountResponse response2 = new AccountResponse();

        when(service.getAllAccountsByUserId(userId)).thenReturn(List.of(response1, response2));

        mockMvc.perform(get("/accounts/user/{userId}", userId))
                .andExpect(status().isOk());
    }

    @Test
    void testIncreaseBalance() throws Exception {
        String iban = "AZ90 QRYR Z4W2 KBDS 4RD0 9Z9B SFFA";
        BigDecimal amount = new BigDecimal("100.00");

        doNothing()
                .when(service)
                .increaseBalance(iban, amount);

        mockMvc.perform(put("/accounts/iban/{iban}/increase", iban)
                .param("amount", amount.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void testDecreaseBalance() throws Exception {
        String iban = "AZ90 QRYR Z4W2 KBDS 4RD0 9Z9B SFFA";
        BigDecimal amount = new BigDecimal("100.00");

        doNothing()
                .when(service)
                .decreaseBalance(iban, amount);

        mockMvc.perform(put("/accounts/iban/{iban}/decrease", iban)
                .param("amount", amount.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdateAccount() throws Exception {
        Long accountId = 1L;

        UpdateAccountRequest request = new UpdateAccountRequest();
        request.setAccountType(AccountType.DEBIT);
        request.setAccountStatus(AccountStatus.ACTIVE);

        AccountResponse response = new AccountResponse();

        when(service.updateAccount(eq(accountId), any(UpdateAccountRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/accounts/{id}", accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteAccount() throws Exception {
        Long accountId = 1L;

        doNothing()
                .when(service)
                .deleteAccount(accountId);

        mockMvc.perform(delete("/accounts/{id}", accountId))
                .andExpect(status().isOk());
    }

}