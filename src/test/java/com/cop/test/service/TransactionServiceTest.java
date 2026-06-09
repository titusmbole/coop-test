package com.cop.test.service;

import com.cop.test.dto.request.TransferRequest;
import com.cop.test.dto.response.TransferResponse;
import com.cop.test.exception.InsufficientFundsException;
import com.cop.test.exception.InvalidTransactionException;
import com.cop.test.exception.ResourceNotFoundException;
import com.cop.test.model.Customer;
import com.cop.test.model.Transaction;
import com.cop.test.model.TransactionStatus;
import com.cop.test.model.TransactionType;
import com.cop.test.repository.CustomerRepository;
import com.cop.test.repository.TransactionRepository;
import com.cop.test.service.impl.TransactionServiceImpl;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private TransferRequest transferRequest;
    private Customer sender;
    private Customer receiver;

    @BeforeEach
    void setUp() {
        transferRequest = TransferRequest.builder()
                .senderAccountNumber("ACC_SENDER_001")
                .senderName("John Doe")
                .receiverAccountNumber("ACC_RECEIVER_001")
                .receiverName("Jane Smith")
                .amount(new BigDecimal("1500.00"))
                .currency("KES")
                .description("Test transfer")
                .channel("API")
                .build();

        sender = Customer.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phoneNumber("+254711111111")
                .accountNumber("ACC_SENDER_001")
                .accountBalance(new BigDecimal("10000.00"))
                .createdAt(LocalDateTime.now())
                .build();

        receiver = Customer.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Smith")
                .email("jane@example.com")
                .phoneNumber("+254722222222")
                .accountNumber("ACC_RECEIVER_001")
                .accountBalance(new BigDecimal("5000.00"))
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should transfer funds successfully")
    void transferFunds_Success() {
        when(customerRepository.findByAccountNumberForUpdate("ACC_SENDER_001"))
                .thenReturn(Optional.of(sender));
        when(customerRepository.findByAccountNumberForUpdate("ACC_RECEIVER_001"))
                .thenReturn(Optional.of(receiver));
        when(customerRepository.save(any(Customer.class))).thenAnswer(i -> i.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction txn = invocation.getArgument(0);
            txn.setId(1L);
            txn.setCreatedAt(LocalDateTime.now());
            return txn;
        });

        TransferResponse response = transactionService.transferFunds(transferRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("SUCCESSFUL");
        assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("1500.00"));
        assertThat(response.getSenderAccountNumber()).isEqualTo("ACC_SENDER_001");
        assertThat(response.getReceiverAccountNumber()).isEqualTo("ACC_RECEIVER_001");
        assertThat(response.getSenderName()).isEqualTo("John Doe");
        assertThat(response.getReceiverName()).isEqualTo("Jane Smith");
        assertThat(response.getCurrency()).isEqualTo("KES");
        assertThat(response.getTransactionType()).isEqualTo("TRANSFER");
        assertThat(response.getTransactionReference()).startsWith("TXN");
        assertThat(response.getSenderBalanceAfter()).isEqualByComparingTo(new BigDecimal("8500.00"));
        assertThat(response.getReceiverBalanceAfter()).isEqualByComparingTo(new BigDecimal("6500.00"));

        verify(customerRepository, times(2)).save(any(Customer.class));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should throw exception when transferring to same account")
    void transferFunds_SameAccount() {
        transferRequest.setReceiverAccountNumber("ACC_SENDER_001");

        assertThatThrownBy(() -> transactionService.transferFunds(transferRequest))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessageContaining("same account");

        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    @DisplayName("Should throw exception when sender account not found")
    void transferFunds_SenderNotFound() {
        when(customerRepository.findByAccountNumberForUpdate("ACC_SENDER_001"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.transferFunds(transferRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Sender account");

        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    @DisplayName("Should throw exception when receiver account not found")
    void transferFunds_ReceiverNotFound() {
        when(customerRepository.findByAccountNumberForUpdate("ACC_SENDER_001"))
                .thenReturn(Optional.of(sender));
        when(customerRepository.findByAccountNumberForUpdate("ACC_RECEIVER_001"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.transferFunds(transferRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Receiver account");

        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    @DisplayName("Should throw exception when sender name does not match")
    void transferFunds_SenderNameMismatch() {
        transferRequest.setSenderName("Wrong Name");

        when(customerRepository.findByAccountNumberForUpdate("ACC_SENDER_001"))
                .thenReturn(Optional.of(sender));
        when(customerRepository.findByAccountNumberForUpdate("ACC_RECEIVER_001"))
                .thenReturn(Optional.of(receiver));

        assertThatThrownBy(() -> transactionService.transferFunds(transferRequest))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessageContaining("Sender name")
                .hasMessageContaining("does not match");

        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    @DisplayName("Should throw exception when receiver name does not match")
    void transferFunds_ReceiverNameMismatch() {
        transferRequest.setReceiverName("Wrong Name");

        when(customerRepository.findByAccountNumberForUpdate("ACC_SENDER_001"))
                .thenReturn(Optional.of(sender));
        when(customerRepository.findByAccountNumberForUpdate("ACC_RECEIVER_001"))
                .thenReturn(Optional.of(receiver));

        assertThatThrownBy(() -> transactionService.transferFunds(transferRequest))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessageContaining("Receiver name")
                .hasMessageContaining("does not match");

        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    @DisplayName("Should throw exception when insufficient funds")
    void transferFunds_InsufficientFunds() {
        sender.setAccountBalance(new BigDecimal("500.00")); // Less than transfer amount
        transferRequest.setAmount(new BigDecimal("1500.00"));

        when(customerRepository.findByAccountNumberForUpdate("ACC_SENDER_001"))
                .thenReturn(Optional.of(sender));
        when(customerRepository.findByAccountNumberForUpdate("ACC_RECEIVER_001"))
                .thenReturn(Optional.of(receiver));

        assertThatThrownBy(() -> transactionService.transferFunds(transferRequest))
                .isInstanceOf(InsufficientFundsException.class)
                .hasMessageContaining("Insufficient funds");

        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    @DisplayName("Should debit sender and credit receiver with correct amounts")
    void transferFunds_CorrectBalanceUpdates() {
        when(customerRepository.findByAccountNumberForUpdate("ACC_SENDER_001"))
                .thenReturn(Optional.of(sender));
        when(customerRepository.findByAccountNumberForUpdate("ACC_RECEIVER_001"))
                .thenReturn(Optional.of(receiver));
        when(customerRepository.save(any(Customer.class))).thenAnswer(i -> i.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> {
            Transaction txn = i.getArgument(0);
            txn.setId(1L);
            return txn;
        });

        transactionService.transferFunds(transferRequest);

        // Sender: 10000 - 1500 = 8500
        assertThat(sender.getAccountBalance()).isEqualByComparingTo(new BigDecimal("8500.00"));
        // Receiver: 5000 + 1500 = 6500
        assertThat(receiver.getAccountBalance()).isEqualByComparingTo(new BigDecimal("6500.00"));
    }

    @Test
    @DisplayName("Should throw exception when transfer amount is zero or negative")
    void transferFunds_ZeroAmount() {
        transferRequest.setAmount(BigDecimal.ZERO);

        assertThatThrownBy(() -> transactionService.transferFunds(transferRequest))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessageContaining("greater than zero");
    }
}

