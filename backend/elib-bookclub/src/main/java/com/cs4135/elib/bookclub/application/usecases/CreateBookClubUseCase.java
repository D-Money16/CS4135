package com.cs4135.elib.bookclub.application.usecases;

import com.cs4135.elib.bookclub.domain.BookClub;
import com.cs4135.elib.bookclub.domain.ClubRole;
import com.cs4135.elib.bookclub.dto.BookClubCreatedEvent;
import com.cs4135.elib.bookclub.infrastructure.BookClubRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CreateBookClubUseCase {

    @Autowired
    private BookClubRepository bookClubRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Transactional
    public BookClub execute(String name, String description, UUID ownerId) {
        BookClub club = new BookClub(name, description);
        club.addMember(ownerId, ClubRole.OWNER);
        BookClub saved = bookClubRepository.save(club);
        eventPublisher.publishEvent(new BookClubCreatedEvent(saved.getClubId(), saved.getName(), ownerId));
        return saved;
    }

    public java.util.List<BookClub> findAll() {
        return bookClubRepository.findAll();
    }

    public BookClub findById(UUID clubId) {
        return bookClubRepository.findById(clubId)
            .orElseThrow(() -> new IllegalArgumentException("Book club not found: " + clubId));
    }
}
