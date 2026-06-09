package com.cop.test.controller;

import com.cop.test.dto.request.TransferRequest;
import com.cop.test.dto.response.ApiResponse;
import com.cop.test.dto.response.TransferResponse;
import com.cop.test.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction Management", description = "APIs for managing fund transfers")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transfer")
    @Operation(summary = "Transfer funds", description = "Transfers funds from one customer account to another")
    public ResponseEntity<ApiResponse<TransferResponse>> transferFunds(
            @Valid @RequestBody TransferRequest request) {
        TransferResponse response = transactionService.transferFunds(request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("Funds transferred successfully", response));
    }
}

