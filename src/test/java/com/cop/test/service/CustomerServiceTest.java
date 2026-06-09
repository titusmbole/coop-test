package com.cop.test.service;

import com.cop.test.dto.request.CustomerRequest;
import com.cop.test.dto.response.BalanceResponse;
import com.cop.test.dto.response.CustomerResponse;
import com.cop.test.exception.DuplicateResourceException;
import com.cop.test.exception.ResourceNotFoundException;
import com.cop.test.model.Customer;
import com.cop.test.repository.CustomerRepository;
import com.cop.test.service.impl.CustomerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private CustomerRequest customerRequest;
    private Customer customer;

    @BeforeEach
    void setUp() {
        customerRequest = CustomerRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phoneNumber("+254712345678")
                .initialDeposit(new BigDecimal("5000.00"))
                .build();

        customer = Customer.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phoneNumber("+254712345678")
                .accountNumber("ACC1234567890")
                .accountBalance(new BigDecimal("5000.00"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create customer successfully")
    void createCustomer_Success() {
        when(customerRepository.existsByEmail(anyString())).thenReturn(false);
        when(customerRepository.existsByPhoneNumber(anyString())).thenReturn(false);
        when(customerRepository.existsByAccountNumber(anyString())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        CustomerResponse response = customerService.createCustomer(customerRequest);

        assertThat(response).isNotNull();
        assertThat(response.getFirstName()).isEqualTo("John");
        assertThat(response.getLastName()).isEqualTo("Doe");
        assertThat(response.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(response.getAccountBalance()).isEqualByComparingTo(new BigDecimal("5000.00"));
        assertThat(response.getAccountNumber()).isNotNull();

        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when email already exists")
    void createCustomer_DuplicateEmail() {
        when(customerRepository.existsByEmail("john.doe@example.com")).thenReturn(true);

        assertThatThrownBy(() -> customerService.createCustomer(customerRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("email");

        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when phone number already exists")
    void createCustomer_DuplicatePhone() {
        when(customerRepository.existsByEmail(anyString())).thenReturn(false);
        when(customerRepository.existsByPhoneNumber("+254712345678")).thenReturn(true);

        assertThatThrownBy(() -> customerService.createCustomer(customerRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("phone number");

        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    @DisplayName("Should return account balance successfully")
    void getAccountBalance_Success() {
        when(customerRepository.findByAccountNumber("ACC1234567890")).thenReturn(Optional.of(customer));

        BalanceResponse response = customerService.getAccountBalance("ACC1234567890");

        assertThat(response).isNotNull();
        assertThat(response.getAccountNumber()).isEqualTo("ACC1234567890");
        assertThat(response.getCustomerName()).isEqualTo("John Doe");
        assertThat(response.getAvailableBalance()).isEqualByComparingTo(new BigDecimal("5000.00"));
        assertThat(response.getCurrency()).isEqualTo("KES");
        assertThat(response.getEnquiryDate()).isNotNull();
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when account not found")
    void getAccountBalance_AccountNotFound() {
        when(customerRepository.findByAccountNumber("INVALID")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.getAccountBalance("INVALID"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }
}

