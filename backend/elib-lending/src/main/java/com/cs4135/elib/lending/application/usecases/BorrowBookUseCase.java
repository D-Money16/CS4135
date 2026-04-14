package com.cs4135.elib.lending.application.usecases;

import com.cs4135.elib.lending.application.acl.CatalogueClient;
import com.cs4135.elib.lending.domain.Loan;
import com.cs4135.elib.lending.domain.LoanStatus;
import com.cs4135.elib.lending.infrastructure.LoanRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class BorrowBookUseCase {

    private final CatalogueClient catalogueClient;
    private final LoanRepository loanRepository;

    public BorrowBookUseCase(CatalogueClient catalogueClient, LoanRepository loanRepository) {
        this.catalogueClient = catalogueClient;
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

        return loanRepository.save(loan);
    }
}
