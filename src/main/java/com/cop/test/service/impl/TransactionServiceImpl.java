package com.cop.test.service.impl;

import com.cop.test.dto.request.TransferRequest;
import com.cop.test.dto.response.TransferResponse;
import com.cop.test.exception.InsufficientFundsException;
import com.cop.test.exception.ResourceNotFoundException;
import com.cop.test.exception.InvalidTransactionException;
import com.cop.test.model.Customer;
import com.cop.test.model.Transaction;
import com.cop.test.model.TransactionStatus;
import com.cop.test.model.TransactionType;
import com.cop.test.repository.CustomerRepository;
import com.cop.test.repository.TransactionRepository;
import com.cop.test.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Transfers funds between two accounts.
     * Uses SERIALIZABLE isolation to prevent concurrent modification issues.
     * Rolls back on ANY exception to ensure data integrity.
     */
    @Override
    @Transactional(
            propagation = Propagation.REQUIRED,
            isolation = Isolation.SERIALIZABLE,
            rollbackFor = Exception.class,
            timeout = 30
    )
    public TransferResponse transferFunds(TransferRequest request) {
        log.info("Initiating fund transfer from {} to {} for amount {} {}",
                request.getSenderAccountNumber(),
                request.getReceiverAccountNumber(),
                request.getAmount(),
                request.getCurrency());

        // Validate sender and receiver are not the same
        if (request.getSenderAccountNumber().equals(request.getReceiverAccountNumber())) {
            throw new InvalidTransactionException("Cannot transfer funds to the same account");
        }

        // Validate amount is positive
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Transfer amount must be greater than zero");
        }

        // Fetch sender (with pessimistic lock to prevent concurrent modifications)
        Customer sender = customerRepository.findByAccountNumberForUpdate(request.getSenderAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sender account " + request.getSenderAccountNumber() + " not found"));

        // Fetch receiver (with pessimistic lock to prevent concurrent modifications)
        Customer receiver = customerRepository.findByAccountNumberForUpdate(request.getReceiverAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Receiver account " + request.getReceiverAccountNumber() + " not found"));

        // Validate sender name matches the account holder
        String senderFullName = sender.getFirstName() + " " + sender.getLastName();
        if (!senderFullName.equalsIgnoreCase(request.getSenderName().trim())) {
            log.warn("Sender name mismatch. Expected: '{}', Provided: '{}'", senderFullName, request.getSenderName());
            throw new InvalidTransactionException(
                    "Sender name '" + request.getSenderName() + "' does not match the account holder for account " + request.getSenderAccountNumber());
        }

        // Validate receiver name matches the account holder
        String receiverFullName = receiver.getFirstName() + " " + receiver.getLastName();
        if (!receiverFullName.equalsIgnoreCase(request.getReceiverName().trim())) {
            log.warn("Receiver name mismatch. Expected: '{}', Provided: '{}'", receiverFullName, request.getReceiverName());
            throw new InvalidTransactionException(
                    "Receiver name '" + request.getReceiverName() + "' does not match the account holder for account " + request.getReceiverAccountNumber());
        }

        // Validate sufficient balance
        if (sender.getAccountBalance().compareTo(request.getAmount()) < 0) {
            log.warn("Insufficient funds in account {}. Balance: {}, Requested: {}",
                    sender.getAccountNumber(), sender.getAccountBalance(), request.getAmount());

            // Record FAILED transaction before throwing
            recordFailedTransaction(request, "Insufficient funds");

            throw new InsufficientFundsException(
                    "Insufficient funds. Available balance: " + sender.getAccountBalance());
        }

        // Record balances before transfer
        BigDecimal senderBalanceBefore = sender.getAccountBalance();
        BigDecimal receiverBalanceBefore = receiver.getAccountBalance();

        // Perform the debit and credit atomically (if any fails, entire transaction rolls back)
        sender.setAccountBalance(senderBalanceBefore.subtract(request.getAmount()));
        receiver.setAccountBalance(receiverBalanceBefore.add(request.getAmount()));

        customerRepository.save(sender);
        customerRepository.save(receiver);

        // Record the successful transaction
        String transactionRef = "TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();

        Transaction transaction = Transaction.builder()
                .transactionReference(transactionRef)
                .senderAccountNumber(request.getSenderAccountNumber())
                .senderName(senderFullName)
                .receiverAccountNumber(request.getReceiverAccountNumber())
                .receiverName(receiverFullName)
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(TransactionStatus.SUCCESSFUL)
                .transactionType(TransactionType.TRANSFER)
                .description(request.getDescription() != null ? request.getDescription() : "Fund Transfer")
                .channel(request.getChannel() != null ? request.getChannel() : "API")
                .senderBalanceBefore(senderBalanceBefore)
                .senderBalanceAfter(sender.getAccountBalance())
                .receiverBalanceBefore(receiverBalanceBefore)
                .receiverBalanceAfter(receiver.getAccountBalance())
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Transfer successful. Reference: {}. Sender balance: {} -> {}, Receiver balance: {} -> {}",
                transactionRef, senderBalanceBefore, sender.getAccountBalance(),
                receiverBalanceBefore, receiver.getAccountBalance());

        return TransferResponse.builder()
                .transactionReference(savedTransaction.getTransactionReference())
                .senderAccountNumber(savedTransaction.getSenderAccountNumber())
                .senderName(savedTransaction.getSenderName())
                .receiverAccountNumber(savedTransaction.getReceiverAccountNumber())
                .receiverName(savedTransaction.getReceiverName())
                .amount(savedTransaction.getAmount())
                .currency(savedTransaction.getCurrency())
                .transactionType(savedTransaction.getTransactionType().name())
                .status(savedTransaction.getStatus().name())
                .description(savedTransaction.getDescription())
                .channel(savedTransaction.getChannel())
                .senderBalanceAfter(savedTransaction.getSenderBalanceAfter())
                .receiverBalanceAfter(savedTransaction.getReceiverBalanceAfter())
                .transactionDate(LocalDateTime.now())
                .build();
    }

    /**
     * Records a failed transaction in a NEW transaction context.
     * Uses REQUIRES_NEW so even if the parent transaction rolls back,
     * the failed transaction record is still persisted for auditing.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void recordFailedTransaction(TransferRequest request, String reason) {
        try {
            String transactionRef = "TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();

            Transaction failedTransaction = Transaction.builder()
                    .transactionReference(transactionRef)
                    .senderAccountNumber(request.getSenderAccountNumber())
                    .senderName(request.getSenderName())
                    .receiverAccountNumber(request.getReceiverAccountNumber())
                    .receiverName(request.getReceiverName())
                    .amount(request.getAmount())
                    .currency(request.getCurrency())
                    .status(TransactionStatus.FAILED)
                    .transactionType(TransactionType.TRANSFER)
                    .description("FAILED: " + reason)
                    .channel(request.getChannel() != null ? request.getChannel() : "API")
                    .build();

            transactionRepository.save(failedTransaction);
            log.warn("Failed transaction recorded. Reference: {}, Reason: {}", transactionRef, reason);
        } catch (Exception e) {
            log.error("Could not record failed transaction: {}", e.getMessage());
        }
    }
}

