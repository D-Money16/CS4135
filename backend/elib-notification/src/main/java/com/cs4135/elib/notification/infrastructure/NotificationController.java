package com.cs4135.elib.notification.infrastructure;

import com.cs4135.elib.notification.application.acl.LendingEventTranslator;
import com.cs4135.elib.notification.application.usecases.DispatchNotificationUseCase;
import com.cs4135.elib.notification.domain.Notification;
import com.cs4135.elib.notification.dto.LoanCreatedEvent;
import com.cs4135.elib.notification.dto.LoanOverdueEvent;
import com.cs4135.elib.notification.dto.NotificationRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final DispatchNotificationUseCase dispatchNotificationUseCase;
    private final LendingEventTranslator lendingEventTranslator;

    public NotificationController(DispatchNotificationUseCase dispatchNotificationUseCase,
                                  LendingEventTranslator lendingEventTranslator) {
        this.dispatchNotificationUseCase = dispatchNotificationUseCase;
        this.lendingEventTranslator = lendingEventTranslator;
    }

    @PostMapping("/lending/loan-created")
    public ResponseEntity<Notification> handleLoanCreated(@RequestBody LoanCreatedEvent event) {
        NotificationRequest request = lendingEventTranslator.fromLoanCreated(event);
        Notification notification = dispatchNotificationUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.OK).body(notification);
    }

    @PostMapping("/lending/loan-overdue")
    public ResponseEntity<Notification> handleLoanOverdue(@RequestBody LoanOverdueEvent event) {
        NotificationRequest request = lendingEventTranslator.fromLoanOverdue(event);
        Notification notification = dispatchNotificationUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.OK).body(notification);
    }

    @PostMapping("/lending/direct")
    public ResponseEntity<Notification> handleDirectLendingNotification(@RequestBody NotificationRequest request) {
        if (request.getSource() == null || request.getSource().isBlank()) {
            request.setSource("LENDING");
        }
        Notification notification = dispatchNotificationUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.OK).body(notification);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}