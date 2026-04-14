package com.cs4135.elib.lending.application.usecases;

import com.cs4135.elib.lending.domain.Loan;
import com.cs4135.elib.lending.domain.LoanStatus;
import com.cs4135.elib.lending.infrastructure.LoanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class OverdueDetectionService {

    private static final Logger log = LoggerFactory.getLogger(OverdueDetectionService.class);

    private final LoanRepository loanRepository;

    public OverdueDetectionService(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    // Runs every day at midnight
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void markOverdueLoans() {
        List<Loan> overdue = loanRepository.findByStatusAndDueDateBefore(
                LoanStatus.ACTIVE, LocalDate.now());

        if (overdue.isEmpty()) return;

        for (Loan loan : overdue) {
            loan.setStatus(LoanStatus.OVERDUE);
            loanRepository.save(loan);
            log.info("Loan {} marked OVERDUE (due {})", loan.getId(), loan.getDueDate());
        }
    }
}
