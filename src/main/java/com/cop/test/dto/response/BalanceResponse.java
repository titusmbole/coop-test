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
public class BalanceResponse {

    private String accountNumber;
    private String customerName;
    private BigDecimal availableBalance;
    private String currency;
    private LocalDateTime enquiryDate;
}

