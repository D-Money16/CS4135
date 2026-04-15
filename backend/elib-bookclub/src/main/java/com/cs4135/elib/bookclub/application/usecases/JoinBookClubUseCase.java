package com.cs4135.elib.bookclub.application.usecases;

import com.cs4135.elib.bookclub.domain.BookClub;
import com.cs4135.elib.bookclub.domain.ClubRole;
import com.cs4135.elib.bookclub.dto.MemberJoinedEvent;
import com.cs4135.elib.bookclub.infrastructure.BookClubRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class JoinBookClubUseCase {

    @Autowired
    private BookClubRepository bookClubRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Transactional
    public BookClub execute(UUID clubId, UUID userId) {
        BookClub club = bookClubRepository.findById(clubId)
            .orElseThrow(() -> new IllegalArgumentException("Book club not found: " + clubId));

        club.addMember(userId, ClubRole.MEMBER);
        BookClub saved = bookClubRepository.save(club);

        eventPublisher.publishEvent(
            new MemberJoinedEvent(clubId, saved.getName(), userId, LocalDateTime.now())
        );

        return saved;
    }
}
