package com.cop.test.controller;

import com.cop.test.dto.request.CustomerRequest;
import com.cop.test.dto.response.ApiResponse;
import com.cop.test.dto.response.BalanceResponse;
import com.cop.test.dto.response.CustomerResponse;
import com.cop.test.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Customer Management", description = "APIs for managing customer accounts")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @Operation(summary = "Create a new customer account", description = "Registers a new customer and creates their account with an initial deposit")
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(
            @Valid @RequestBody CustomerRequest request) {
        CustomerResponse response = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Customer account created successfully", response));
    }

    @GetMapping("/{accountNumber}/balance")
    @Operation(summary = "Get account balance", description = "Retrieves the current account balance for a given account number")
    public ResponseEntity<ApiResponse<BalanceResponse>> getAccountBalance(
            @PathVariable String accountNumber) {
        BalanceResponse response = customerService.getAccountBalance(accountNumber);
        return ResponseEntity.ok(ApiResponse.success("Account balance retrieved successfully", response));
    }
}

