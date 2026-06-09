package com.cop.test.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferResponse {

    private String transactionReference;
    private String senderAccountNumber;
    private String senderName;
    private String receiverAccountNumber;
    private String receiverName;
    private BigDecimal amount;
    private String currency;
    private String transactionType;
    private String status;
    private String description;
    private String channel;
    private BigDecimal senderBalanceAfter;
    private BigDecimal receiverBalanceAfter;
    private LocalDateTime transactionDate;
}

