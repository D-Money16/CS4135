package com.cs4135.elib.lending.application.usecases;

import com.cs4135.elib.lending.application.acl.CatalogueClient;
import com.cs4135.elib.lending.application.acl.NotificationClient;
import com.cs4135.elib.lending.domain.Loan;
import com.cs4135.elib.lending.domain.LoanStatus;
import com.cs4135.elib.lending.infrastructure.LoanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class ReturnBookUseCase {

    private static final Logger log = LoggerFactory.getLogger(ReturnBookUseCase.class);

    private final CatalogueClient catalogueClient;
    private final NotificationClient notificationClient;
    private final LoanRepository loanRepository;

    public ReturnBookUseCase(CatalogueClient catalogueClient,
                             NotificationClient notificationClient,
                             LoanRepository loanRepository) {
        this.catalogueClient = catalogueClient;
        this.notificationClient = notificationClient;
        this.loanRepository = loanRepository;
    }

    public Loan execute(UUID loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found: " + loanId));

        if (loan.getStatus() == LoanStatus.RETURNED) {
            throw new RuntimeException("Loan already returned: " + loanId);
        }

        catalogueClient.releaseCopy(loan.getCopyId());

        loan.setStatus(LoanStatus.RETURNED);
        loan.setReturnedAt(LocalDate.now());

        Loan savedLoan = loanRepository.save(loan);

        try {
            notificationClient.sendDirectNotification(
                    savedLoan.getUserId(),
                    savedLoan.getId(),
                    "RETURN_CONFIRMATION",
                    "Return confirmed successfully."
            );
        } catch (Exception ex) {
            log.warn("Notification service unavailable for return {}. Return succeeded without notification.",
                    savedLoan.getId(), ex);
        }

        return savedLoan;
    }
}