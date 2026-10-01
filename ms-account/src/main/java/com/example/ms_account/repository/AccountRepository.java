package com.example.ms_account.repository;

import com.example.ms_account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByIban(String iban);

    List<Account> findByUserId(Long userId);

    boolean existsById(Long id);
}
