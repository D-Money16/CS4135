package com.cs4135.elib.lending.infrastructure;

import com.cs4135.elib.lending.domain.Loan;
import com.cs4135.elib.lending.domain.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface LoanRepository extends JpaRepository<Loan, UUID> {
    List<Loan> findByUserId(UUID userId);
    List<Loan> findByUserIdAndStatus(UUID userId, LoanStatus status);
    List<Loan> findByStatusAndDueDateBefore(LoanStatus status, LocalDate date);
}
