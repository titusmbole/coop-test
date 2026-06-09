package com.cop.test.repository;

import com.cop.test.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findBySenderAccountNumberOrReceiverAccountNumberOrderByCreatedAtDesc(
            String senderAccount, String receiverAccount);
}

