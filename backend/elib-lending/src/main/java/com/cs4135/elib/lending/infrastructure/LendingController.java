package com.cs4135.elib.lending.infrastructure;

import com.cs4135.elib.lending.application.usecases.BorrowBookUseCase;
import com.cs4135.elib.lending.application.usecases.ReturnBookUseCase;
import com.cs4135.elib.lending.domain.Loan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/lending")
public class LendingController {

    private final BorrowBookUseCase borrowBookUseCase;
    private final ReturnBookUseCase returnBookUseCase;
    private final LoanRepository loanRepository;

    public LendingController(BorrowBookUseCase borrowBookUseCase,
                             ReturnBookUseCase returnBookUseCase,
                             LoanRepository loanRepository) {
        this.borrowBookUseCase = borrowBookUseCase;
        this.returnBookUseCase = returnBookUseCase;
        this.loanRepository = loanRepository;
    }

    @PostMapping("/borrow")
    public ResponseEntity<?> borrow(@RequestBody BorrowRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(borrowBookUseCase.execute(request.userId(), request.bookId()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/loans/{loanId}/return")
    public ResponseEntity<?> returnBook(@PathVariable UUID loanId) {
        try {
            return ResponseEntity.ok(returnBookUseCase.execute(loanId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/loans/user/{userId}")
    public ResponseEntity<List<Loan>> getUserLoans(@PathVariable UUID userId) {
        return ResponseEntity.ok(loanRepository.findByUserId(userId));
    }

    record BorrowRequest(UUID userId, UUID bookId) {}
}
