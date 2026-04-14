package com.cs4135.elib.lending.application.usecases;

import com.cs4135.elib.lending.application.acl.CatalogueClient;
import com.cs4135.elib.lending.domain.Loan;
import com.cs4135.elib.lending.domain.LoanStatus;
import com.cs4135.elib.lending.infrastructure.LoanRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class ReturnBookUseCase {

    private final CatalogueClient catalogueClient;
    private final LoanRepository loanRepository;

    public ReturnBookUseCase(CatalogueClient catalogueClient, LoanRepository loanRepository) {
        this.catalogueClient = catalogueClient;
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

        return loanRepository.save(loan);
    }
}
