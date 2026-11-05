package com.example.ms_account.controller;

import com.example.ms_account.dto.request.CreateAccountRequest;
import com.example.ms_account.dto.request.UpdateAccountRequest;
import com.example.ms_account.dto.response.AccountResponse;
import com.example.ms_account.enums.AccountStatus;
import com.example.ms_account.enums.AccountType;
import com.example.ms_account.security.JwtService;
import com.example.ms_account.service.AccountService;
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

import static com.example.ms_account.util.TestConstants.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService service;

    @MockitoBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TOKEN = "token123";

    @Test
    void testCreateAccount() throws Exception {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setUserId(USER_ID);
        request.setAccountType(AccountType.DEBIT);

        AccountResponse response = new AccountResponse();

        when(service.createAccount(any(CreateAccountRequest.class), eq(TOKEN)))
                .thenReturn(response);

        mockMvc.perform(post("/accounts")
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAccountById() throws Exception {
        AccountResponse response = new AccountResponse();

        when(service.getAccountById(ACCOUNT_ID)).thenReturn(response);

        mockMvc.perform(get("/accounts/{id}", ACCOUNT_ID))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAccountByIban() throws Exception {
        AccountResponse response = new AccountResponse();

        when(service.getAccountByIban(IBAN)).thenReturn(response);

        mockMvc.perform(get("/accounts/iban/{iban}", IBAN))
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
        AccountResponse response1 = new AccountResponse();
        AccountResponse response2 = new AccountResponse();

        when(service.getAllAccountsByUserId(USER_ID)).thenReturn(List.of(response1, response2));

        mockMvc.perform(get("/accounts/user/{userId}", USER_ID))
                .andExpect(status().isOk());
    }

    @Test
    void testIncreaseBalance() throws Exception {
        BigDecimal amount = new BigDecimal("100.00");

        doNothing()
                .when(service)
                .increaseBalance(IBAN, amount);

        mockMvc.perform(put("/accounts/iban/{iban}/increase", IBAN)
                .param("amount", amount.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void testDecreaseBalance() throws Exception {
        BigDecimal amount = new BigDecimal("100.00");

        doNothing()
                .when(service)
                .decreaseBalance(IBAN, amount);

        mockMvc.perform(put("/accounts/iban/{iban}/decrease", IBAN)
                .param("amount", amount.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdateAccount() throws Exception {
        UpdateAccountRequest request = new UpdateAccountRequest();
        request.setAccountType(AccountType.DEBIT);
        request.setAccountStatus(AccountStatus.ACTIVE);

        AccountResponse response = new AccountResponse();

        when(service.updateAccount(eq(ACCOUNT_ID), any(UpdateAccountRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/accounts/{id}", ACCOUNT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteAccount() throws Exception {
        doNothing()
                .when(service)
                .deleteAccount(ACCOUNT_ID);

        mockMvc.perform(delete("/accounts/{id}", ACCOUNT_ID))
                .andExpect(status().isOk());
    }

}