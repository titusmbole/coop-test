package com.cop.test.controller;

import com.cop.test.dto.request.CustomerRequest;
import com.cop.test.dto.response.ApiResponse;
import com.cop.test.dto.response.BalanceResponse;
import com.cop.test.dto.response.CustomerResponse;
import com.cop.test.exception.GlobalExceptionHandler;
import com.cop.test.exception.ResourceNotFoundException;
import com.cop.test.service.CustomerService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerService customerService;

    @Test
    @DisplayName("POST /api/v1/customers - Should create customer successfully")
    void createCustomer_Success() throws Exception {
        CustomerRequest request = CustomerRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phoneNumber("+254712345678")
                .initialDeposit(new BigDecimal("5000.00"))
                .build();

        CustomerResponse response = CustomerResponse.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .accountNumber("ACC1234567890")
                .accountBalance(new BigDecimal("5000.00"))
                .email("john@example.com")
                .phoneNumber("+254712345678")
                .createdAt(LocalDateTime.now())
                .build();

        when(customerService.createCustomer(any(CustomerRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Customer account created successfully"))
                .andExpect(jsonPath("$.data.firstName").value("John"))
                .andExpect(jsonPath("$.data.lastName").value("Doe"))
                .andExpect(jsonPath("$.data.accountNumber").value("ACC1234567890"))
                .andExpect(jsonPath("$.data.accountBalance").value(5000.00));
    }

    @Test
    @DisplayName("POST /api/v1/customers - Should return 400 when email is invalid")
    void createCustomer_InvalidEmail() throws Exception {
        CustomerRequest request = CustomerRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("invalid-email")
                .phoneNumber("+254712345678")
                .initialDeposit(new BigDecimal("5000.00"))
                .build();

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/v1/customers - Should return 400 when required fields are missing")
    void createCustomer_MissingFields() throws Exception {
        CustomerRequest request = CustomerRequest.builder()
                .firstName("")
                .lastName("")
                .email("")
                .phoneNumber("")
                .build();

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    @DisplayName("GET /api/v1/customers/{accountNumber}/balance - Should return balance")
    void getBalance_Success() throws Exception {
        BalanceResponse balanceResponse = BalanceResponse.builder()
                .accountNumber("ACC1234567890")
                .customerName("John Doe")
                .availableBalance(new BigDecimal("5000.00"))
                .currency("KES")
                .enquiryDate(LocalDateTime.now())
                .build();

        when(customerService.getAccountBalance("ACC1234567890")).thenReturn(balanceResponse);

        mockMvc.perform(get("/api/v1/customers/ACC1234567890/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accountNumber").value("ACC1234567890"))
                .andExpect(jsonPath("$.data.customerName").value("John Doe"))
                .andExpect(jsonPath("$.data.availableBalance").value(5000.00))
                .andExpect(jsonPath("$.data.currency").value("KES"));
    }

    @Test
    @DisplayName("GET /api/v1/customers/{accountNumber}/balance - Should return 404 for unknown account")
    void getBalance_NotFound() throws Exception {
        when(customerService.getAccountBalance("INVALID"))
                .thenThrow(new ResourceNotFoundException("Customer with account number INVALID not found"));

        mockMvc.perform(get("/api/v1/customers/INVALID/balance"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Customer with account number INVALID not found"));
    }
}

