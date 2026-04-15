package com.cs4135.elib.lending.application.usecases;

import com.cs4135.elib.lending.application.acl.CatalogueClient;
import com.cs4135.elib.lending.application.acl.NotificationClient;
import com.cs4135.elib.lending.domain.Loan;
import com.cs4135.elib.lending.domain.LoanStatus;
import com.cs4135.elib.lending.dto.LoanCreatedEvent;
import com.cs4135.elib.lending.infrastructure.LoanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class BorrowBookUseCase {

    private static final Logger log = LoggerFactory.getLogger(BorrowBookUseCase.class);

    private final CatalogueClient catalogueClient;
    private final NotificationClient notificationClient;
    private final LoanRepository loanRepository;

    public BorrowBookUseCase(CatalogueClient catalogueClient,
                             NotificationClient notificationClient,
                             LoanRepository loanRepository) {
        this.catalogueClient = catalogueClient;
        this.notificationClient = notificationClient;
        this.loanRepository = loanRepository;
    }

    public Loan execute(UUID userId, UUID bookId) {
        long activeLoans = loanRepository.findByUserIdAndStatus(userId, LoanStatus.ACTIVE).size()
                + loanRepository.findByUserIdAndStatus(userId, LoanStatus.OVERDUE).size();

        if (activeLoans >= 5) {
            throw new IllegalStateException("User has reached the maximum of 5 active loans");
        }

        UUID copyId = catalogueClient.reserveCopy(bookId);

        Loan loan = new Loan();
        loan.setUserId(userId);
        loan.setBookId(bookId);
        loan.setCopyId(copyId);
        loan.setBorrowedAt(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusDays(14));
        loan.setStatus(LoanStatus.ACTIVE);

        Loan savedLoan = loanRepository.save(loan);

        LoanCreatedEvent event = new LoanCreatedEvent();
        event.setLoanId(savedLoan.getId());
        event.setUserId(savedLoan.getUserId());
        event.setCopyId(savedLoan.getCopyId());
        event.setBookTitle("Unknown Title");
        event.setDueDate(savedLoan.getDueDate());
        event.setTimestamp(LocalDateTime.now());

        try {
            notificationClient.sendLoanCreatedNotification(event);
        } catch (Exception ex) {
            log.warn("Notification service unavailable for loan {}. Borrow succeeded without notification.",
                    savedLoan.getId(), ex);
        }

        return savedLoan;
    }
}