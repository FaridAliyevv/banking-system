package com.example.ms_transaction.repository;

import com.example.ms_transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByFromIbanOrToIban(
            String fromIban,
            String toIban);
}
