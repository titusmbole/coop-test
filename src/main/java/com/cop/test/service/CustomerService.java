package com.cop.test.service;

import com.cop.test.dto.request.CustomerRequest;
import com.cop.test.dto.response.BalanceResponse;
import com.cop.test.dto.response.CustomerResponse;

public interface CustomerService {

    CustomerResponse createCustomer(CustomerRequest request);

    BalanceResponse getAccountBalance(String accountNumber);
}

