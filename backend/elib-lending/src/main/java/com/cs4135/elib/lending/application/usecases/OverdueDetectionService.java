package com.cs4135.elib.lending.application.usecases;

import com.cs4135.elib.lending.application.acl.NotificationClient;
import com.cs4135.elib.lending.domain.Loan;
import com.cs4135.elib.lending.domain.LoanStatus;
import com.cs4135.elib.lending.dto.LoanOverdueEvent;
import com.cs4135.elib.lending.infrastructure.LoanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class OverdueDetectionService {

    private static final Logger log = LoggerFactory.getLogger(OverdueDetectionService.class);

    private final LoanRepository loanRepository;
    private final NotificationClient notificationClient;

    public OverdueDetectionService(LoanRepository loanRepository,
                                   NotificationClient notificationClient) {
        this.loanRepository = loanRepository;
        this.notificationClient = notificationClient;
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void markOverdueLoans() {
        List<Loan> overdue = loanRepository.findByStatusAndDueDateBefore(
                LoanStatus.ACTIVE, LocalDate.now());

        if (overdue.isEmpty()) return;

        for (Loan loan : overdue) {
            loan.setStatus(LoanStatus.OVERDUE);
            loanRepository.save(loan);

            LoanOverdueEvent event = new LoanOverdueEvent();
            event.setLoanId(loan.getId());
            event.setUserId(loan.getUserId());
            event.setCopyId(loan.getCopyId());
            event.setBookTitle("Unknown Title"); // replace later if title becomes available
            event.setDueDate(loan.getDueDate());
            event.setDaysOverdue((int) ChronoUnit.DAYS.between(loan.getDueDate(), LocalDate.now()));
            event.setTimestamp(LocalDateTime.now());

            notificationClient.sendLoanOverdueNotification(event);

            log.info("Loan {} marked OVERDUE (due {})", loan.getId(), loan.getDueDate());
        }
    }
}