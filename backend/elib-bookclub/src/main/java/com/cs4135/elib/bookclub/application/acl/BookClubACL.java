package com.cs4135.elib.bookclub.application.acl;

import com.cs4135.elib.bookclub.application.usecases.*;
import com.cs4135.elib.bookclub.domain.BookClub;
import com.cs4135.elib.bookclub.domain.ClubRole;
import com.cs4135.elib.bookclub.domain.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Anti-Corruption Layer for the Book Club context.
 * Provides a stable facade for other bounded contexts (e.g. Notification, Lending)
 * to interact with Book Club without coupling to its internal use cases directly.
 */
@Component
public class BookClubACL {

    @Autowired private CreateBookClubUseCase createBookClubUseCase;
    @Autowired private JoinBookClubUseCase joinBookClubUseCase;
    @Autowired private LeaveBookClubUseCase leaveBookClubUseCase;
    @Autowired private ChangeMemberRoleUseCase changeMemberRoleUseCase;
    @Autowired private RemoveMemberUseCase removeMemberUseCase;
    @Autowired private PostMessageUseCase postMessageUseCase;
    @Autowired private EditMessageUseCase editMessageUseCase;
    @Autowired private DeleteMessageUseCase deleteMessageUseCase;

    public BookClub createClub(String name, String description, UUID ownerId) {
        return createBookClubUseCase.execute(name, description, ownerId);
    }

    public BookClub joinClub(UUID clubId, UUID userId) {
        return joinBookClubUseCase.execute(clubId, userId);
    }

    public void leaveClub(UUID clubId, UUID userId) {
        leaveBookClubUseCase.execute(clubId, userId);
    }

    public void changeMemberRole(UUID clubId, UUID requesterId, UUID targetUserId, ClubRole newRole) {
        changeMemberRoleUseCase.execute(clubId, requesterId, targetUserId, newRole);
    }

    public void removeMember(UUID clubId, UUID requesterId, UUID targetUserId) {
        removeMemberUseCase.execute(clubId, requesterId, targetUserId);
    }

    public Message postMessage(UUID clubId, UUID authorUserId, String content) {
        return postMessageUseCase.execute(clubId, authorUserId, content);
    }

    public Message editMessage(UUID messageId, UUID requesterId, String newContent) {
        return editMessageUseCase.execute(messageId, requesterId, newContent);
    }

    public void deleteMessage(UUID messageId, UUID requesterId) {
        deleteMessageUseCase.execute(messageId, requesterId);
    }
}
