package com.cop.test.service;

import com.cop.test.dto.request.TransferRequest;
import com.cop.test.dto.response.TransferResponse;

public interface TransactionService {

    TransferResponse transferFunds(TransferRequest request);
}

