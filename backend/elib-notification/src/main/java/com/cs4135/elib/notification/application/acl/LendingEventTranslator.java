package com.cs4135.elib.notification.application.acl;

import com.cs4135.elib.notification.domain.NotificationType;
import com.cs4135.elib.notification.dto.LoanCreatedEvent;
import com.cs4135.elib.notification.dto.LoanOverdueEvent;
import com.cs4135.elib.notification.dto.NotificationRequest;
import org.springframework.stereotype.Component;

@Component
public class LendingEventTranslator {

    public NotificationRequest fromLoanCreated(LoanCreatedEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("LoanCreatedEvent must not be null.");
        }

        NotificationRequest request = new NotificationRequest();
        request.setUserId(event.getUserId());
        request.setReferenceId(event.getLoanId());
        request.setType(NotificationType.BORROW_CONFIRMATION.name());
        request.setSource("LENDING");
        request.setMessage(buildBorrowConfirmationMessage(event));
        return request;
    }

    public NotificationRequest fromLoanOverdue(LoanOverdueEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("LoanOverdueEvent must not be null.");
        }

        NotificationRequest request = new NotificationRequest();
        request.setUserId(event.getUserId());
        request.setReferenceId(event.getLoanId());
        request.setType(NotificationType.OVERDUE_ALERT.name());
        request.setSource("LENDING");
        request.setMessage(buildOverdueMessage(event));
        return request;
    }

    private String buildBorrowConfirmationMessage(LoanCreatedEvent event) {
        return "Borrow confirmed: \"" + event.getBookTitle() + "\" is due on " + event.getDueDate() + ".";
    }

    private String buildOverdueMessage(LoanOverdueEvent event) {
        return "Overdue alert: \"" + event.getBookTitle() + "\" was due on "
                + event.getDueDate() + " and is now " + event.getDaysOverdue() + " day(s) overdue.";
    }
}