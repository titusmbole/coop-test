package com.cop.test.controller;

import com.cop.test.dto.request.TransferRequest;
import com.cop.test.dto.response.TransferResponse;
import com.cop.test.exception.InsufficientFundsException;
import com.cop.test.exception.InvalidTransactionException;
import com.cop.test.exception.ResourceNotFoundException;
import com.cop.test.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TransactionService transactionService;

    @Test
    @DisplayName("POST /api/v1/transactions/transfer - Should transfer funds successfully")
    void transferFunds_Success() throws Exception {
        TransferRequest request = TransferRequest.builder()
                .senderAccountNumber("ACC_SENDER_001")
                .senderName("John Doe")
                .receiverAccountNumber("ACC_RECEIVER_001")
                .receiverName("Jane Smith")
                .amount(new BigDecimal("1500.00"))
                .currency("KES")
                .description("Payment")
                .build();

        TransferResponse response = TransferResponse.builder()
                .transactionReference("TXN123456789ABC")
                .senderAccountNumber("ACC_SENDER_001")
                .senderName("John Doe")
                .receiverAccountNumber("ACC_RECEIVER_001")
                .receiverName("Jane Smith")
                .amount(new BigDecimal("1500.00"))
                .currency("KES")
                .transactionType("TRANSFER")
                .status("SUCCESSFUL")
                .description("Payment")
                .channel("API")
                .senderBalanceAfter(new BigDecimal("8500.00"))
                .receiverBalanceAfter(new BigDecimal("6500.00"))
                .transactionDate(LocalDateTime.now())
                .build();

        when(transactionService.transferFunds(any(TransferRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.transactionReference").value("TXN123456789ABC"))
                .andExpect(jsonPath("$.data.status").value("SUCCESSFUL"))
                .andExpect(jsonPath("$.data.amount").value(1500.00))
                .andExpect(jsonPath("$.data.senderBalanceAfter").value(8500.00))
                .andExpect(jsonPath("$.data.receiverBalanceAfter").value(6500.00));
    }

    @Test
    @DisplayName("POST /api/v1/transactions/transfer - Should return 400 for missing fields")
    void transferFunds_ValidationError() throws Exception {
        TransferRequest request = TransferRequest.builder()
                .senderAccountNumber("")
                .senderName("")
                .receiverAccountNumber("")
                .receiverName("")
                .currency("")
                .build();

        mockMvc.perform(post("/api/v1/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    @DisplayName("POST /api/v1/transactions/transfer - Should return 400 for insufficient funds")
    void transferFunds_InsufficientFunds() throws Exception {
        TransferRequest request = TransferRequest.builder()
                .senderAccountNumber("ACC_SENDER_001")
                .senderName("John Doe")
                .receiverAccountNumber("ACC_RECEIVER_001")
                .receiverName("Jane Smith")
                .amount(new BigDecimal("100000.00"))
                .currency("KES")
                .build();

        when(transactionService.transferFunds(any(TransferRequest.class)))
                .thenThrow(new InsufficientFundsException("Insufficient funds. Available balance: 10000.00"));

        mockMvc.perform(post("/api/v1/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Insufficient funds. Available balance: 10000.00"));
    }

    @Test
    @DisplayName("POST /api/v1/transactions/transfer - Should return 400 for same account transfer")
    void transferFunds_SameAccount() throws Exception {
        TransferRequest request = TransferRequest.builder()
                .senderAccountNumber("ACC_SENDER_001")
                .senderName("John Doe")
                .receiverAccountNumber("ACC_SENDER_001")
                .receiverName("John Doe")
                .amount(new BigDecimal("1000.00"))
                .currency("KES")
                .build();

        when(transactionService.transferFunds(any(TransferRequest.class)))
                .thenThrow(new InvalidTransactionException("Cannot transfer funds to the same account"));

        mockMvc.perform(post("/api/v1/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Cannot transfer funds to the same account"));
    }

    @Test
    @DisplayName("POST /api/v1/transactions/transfer - Should return 404 for unknown account")
    void transferFunds_AccountNotFound() throws Exception {
        TransferRequest request = TransferRequest.builder()
                .senderAccountNumber("ACC_UNKNOWN")
                .senderName("John Doe")
                .receiverAccountNumber("ACC_RECEIVER_001")
                .receiverName("Jane Smith")
                .amount(new BigDecimal("1000.00"))
                .currency("KES")
                .build();

        when(transactionService.transferFunds(any(TransferRequest.class)))
                .thenThrow(new ResourceNotFoundException("Sender account ACC_UNKNOWN not found"));

        mockMvc.perform(post("/api/v1/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Sender account ACC_UNKNOWN not found"));
    }

    @Test
    @DisplayName("POST /api/v1/transactions/transfer - Should return 400 for name mismatch")
    void transferFunds_NameMismatch() throws Exception {
        TransferRequest request = TransferRequest.builder()
                .senderAccountNumber("ACC_SENDER_001")
                .senderName("Wrong Name")
                .receiverAccountNumber("ACC_RECEIVER_001")
                .receiverName("Jane Smith")
                .amount(new BigDecimal("1000.00"))
                .currency("KES")
                .build();

        when(transactionService.transferFunds(any(TransferRequest.class)))
                .thenThrow(new InvalidTransactionException(
                        "Sender name 'Wrong Name' does not match the account holder for account ACC_SENDER_001"));

        mockMvc.perform(post("/api/v1/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(
                        "Sender name 'Wrong Name' does not match the account holder for account ACC_SENDER_001"));
    }
}

