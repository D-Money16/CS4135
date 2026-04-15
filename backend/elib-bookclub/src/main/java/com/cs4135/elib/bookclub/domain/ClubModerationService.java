package com.cs4135.elib.bookclub.domain;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ClubModerationService {

    /**
     * Changes the role of a target member. Only OWNER or MODERATOR may invoke.
     * Only an OWNER may transfer ownership (promote another member to OWNER).
     * The current OWNER's role is automatically demoted to MODERATOR on transfer.
     */
    public void changeMemberRole(BookClub club, UUID requesterId, UUID targetUserId, ClubRole newRole) {
        ClubMembership requester = club.findMembership(requesterId);
        ClubRole requesterRole = requester.getRole();

        if (requesterRole != ClubRole.OWNER && requesterRole != ClubRole.MODERATOR) {
            throw new IllegalStateException("Only OWNER or MODERATOR can change member roles");
        }

        if (newRole == ClubRole.OWNER && requesterRole != ClubRole.OWNER) {
            throw new IllegalStateException("Only the OWNER can transfer ownership");
        }

        ClubMembership target = club.findMembership(targetUserId);

        if (target.getRole() == ClubRole.OWNER && newRole != ClubRole.OWNER) {
            throw new IllegalStateException(
                "Cannot demote the OWNER directly. Transfer ownership to another member first."
            );
        }

        // Transfer ownership: demote the current owner to MODERATOR
        if (newRole == ClubRole.OWNER) {
            requester.setRole(ClubRole.MODERATOR);
        }

        target.setRole(newRole);
    }

    /**
     * Removes a member from the club. Only OWNER or MODERATOR may invoke.
     * The OWNER cannot be removed.
     */
    public void removeMember(BookClub club, UUID requesterId, UUID targetUserId) {
        ClubMembership requester = club.findMembership(requesterId);

        if (requester.getRole() != ClubRole.OWNER && requester.getRole() != ClubRole.MODERATOR) {
            throw new IllegalStateException("Only OWNER or MODERATOR can remove members");
        }

        club.removeMember(targetUserId);
    }
}
