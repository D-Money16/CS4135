package com.cs4135.elib.bookclub.application.usecases;

import com.cs4135.elib.bookclub.domain.BookClub;
import com.cs4135.elib.bookclub.domain.ClubMembership;
import com.cs4135.elib.bookclub.domain.ClubRole;
import com.cs4135.elib.bookclub.infrastructure.BookClubRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class LeaveBookClubUseCase {

    @Autowired
    private BookClubRepository bookClubRepository;

    @Transactional
    public void execute(UUID clubId, UUID userId) {
        BookClub club = bookClubRepository.findById(clubId)
            .orElseThrow(() -> new IllegalArgumentException("Book club not found: " + clubId));

        ClubMembership membership = club.findMembership(userId);

        if (membership.getRole() == ClubRole.OWNER) {
            throw new IllegalStateException(
                "As club OWNER you must transfer ownership to another member before leaving"
            );
        }

        club.removeMember(userId);
        bookClubRepository.save(club);
    }
}
