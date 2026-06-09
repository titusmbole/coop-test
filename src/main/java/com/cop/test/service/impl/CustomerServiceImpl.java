package com.cop.test.service.impl;

import com.cop.test.dto.request.CustomerRequest;
import com.cop.test.dto.response.BalanceResponse;
import com.cop.test.dto.response.CustomerResponse;
import com.cop.test.exception.DuplicateResourceException;
import com.cop.test.exception.ResourceNotFoundException;
import com.cop.test.model.Customer;
import com.cop.test.repository.CustomerRepository;
import com.cop.test.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public CustomerResponse createCustomer(CustomerRequest request) {
        log.info("Creating new customer with email: {}", request.getEmail());

        // Validate email uniqueness
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Customer with email '" + request.getEmail() + "' already exists");
        }

        // Validate phone number uniqueness
        if (customerRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new DuplicateResourceException("Customer with phone number '" + request.getPhoneNumber() + "' already exists");
        }

        // Generate unique account number
        String accountNumber = generateAccountNumber();

        Customer customer = Customer.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .accountNumber(accountNumber)
                .accountBalance(request.getInitialDeposit())
                .build();

        Customer savedCustomer = customerRepository.save(customer);
        log.info("Customer created successfully with account number: {}", accountNumber);

        return mapToResponse(savedCustomer);
    }

    @Override
    @Transactional(readOnly = true)
    public BalanceResponse getAccountBalance(String accountNumber) {
        log.info("Fetching account balance for account: {}", accountNumber);

        Customer customer = customerRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer with account number " + accountNumber + " not found"));

        return BalanceResponse.builder()
                .accountNumber(customer.getAccountNumber())
                .customerName(customer.getFirstName() + " " + customer.getLastName())
                .availableBalance(customer.getAccountBalance())
                .currency("KES")
                .enquiryDate(LocalDateTime.now())
                .build();
    }

    private String generateAccountNumber() {
        String accountNumber;
        do {
            accountNumber = "ACC" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
        } while (customerRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }

    private CustomerResponse mapToResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .accountNumber(customer.getAccountNumber())
                .accountBalance(customer.getAccountBalance())
                .email(customer.getEmail())
                .phoneNumber(customer.getPhoneNumber())
                .createdAt(customer.getCreatedAt())
                .build();
    }
}

