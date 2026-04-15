package com.cs4135.elib.bookclub.application.usecases;

import com.cs4135.elib.bookclub.domain.BookClub;
import com.cs4135.elib.bookclub.domain.ClubModerationService;
import com.cs4135.elib.bookclub.domain.ClubRole;
import com.cs4135.elib.bookclub.infrastructure.BookClubRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ChangeMemberRoleUseCase {

    @Autowired
    private BookClubRepository bookClubRepository;

    @Autowired
    private ClubModerationService clubModerationService;

    @Transactional
    public void execute(UUID clubId, UUID requesterId, UUID targetUserId, ClubRole newRole) {
        BookClub club = bookClubRepository.findById(clubId)
            .orElseThrow(() -> new IllegalArgumentException("Book club not found: " + clubId));

        clubModerationService.changeMemberRole(club, requesterId, targetUserId, newRole);
        bookClubRepository.save(club);
    }
}
