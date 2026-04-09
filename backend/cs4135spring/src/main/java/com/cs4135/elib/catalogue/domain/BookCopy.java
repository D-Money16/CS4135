package com.cs4135.elib.catalogue.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "book_copies")
@Getter
@NoArgsConstructor
public class BookCopy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "book_id")
    private Book book;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AvailabilityStatus status;

    @Column(length = 100)
    private String condition;

    public BookCopy(Book book) {
        this.book = book;
        this.status = AvailabilityStatus.AVAILABLE;
    }

    public void transitionTo(AvailabilityStatus newStatus) {
        if (this.status == AvailabilityStatus.BORROWED && newStatus == AvailabilityStatus.RESERVED) {
            throw new IllegalStateException(
                "BookCopy cannot transition directly from BORROWED to RESERVED; it must pass through AVAILABLE first."
            );
        }
        this.status = newStatus;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }
}
