package com.cs4135.elib.bookclub.infrastructure;

import com.cs4135.elib.bookclub.application.usecases.*;
import com.cs4135.elib.bookclub.domain.BookClub;
import com.cs4135.elib.bookclub.domain.ClubMembership;
import com.cs4135.elib.bookclub.domain.ClubRole;
import com.cs4135.elib.bookclub.domain.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookclub")
public class BookClubController {

    @Autowired private CreateBookClubUseCase createBookClubUseCase;
    @Autowired private JoinBookClubUseCase joinBookClubUseCase;
    @Autowired private LeaveBookClubUseCase leaveBookClubUseCase;
    @Autowired private ChangeMemberRoleUseCase changeMemberRoleUseCase;
    @Autowired private RemoveMemberUseCase removeMemberUseCase;
    @Autowired private PostMessageUseCase postMessageUseCase;
    @Autowired private EditMessageUseCase editMessageUseCase;
    @Autowired private DeleteMessageUseCase deleteMessageUseCase;
    @Autowired private ClubMembershipRepository membershipRepository;
    @Autowired private MessageRepository messageRepository;

    // ── Clubs ──────────────────────────────────────────────────────────────

    @PostMapping("/clubs")
    public BookClub createClub(
        @RequestHeader("X-User-Id") UUID userId,
        @RequestBody CreateClubRequest request
    ) {
        return createBookClubUseCase.execute(request.name(), request.description(), userId);
    }

    @GetMapping("/clubs")
    public List<BookClub> listClubs() {
        return createBookClubUseCase.findAll();
    }

    @GetMapping("/clubs/{clubId}")
    public BookClub getClub(@PathVariable UUID clubId) {
        return createBookClubUseCase.findById(clubId);
    }

    @PostMapping("/clubs/{clubId}/join")
    public ResponseEntity<Void> joinClub(
        @PathVariable UUID clubId,
        @RequestHeader("X-User-Id") UUID userId
    ) {
        joinBookClubUseCase.execute(clubId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/clubs/{clubId}/leave")
    public ResponseEntity<Void> leaveClub(
        @PathVariable UUID clubId,
        @RequestHeader("X-User-Id") UUID userId
    ) {
        leaveBookClubUseCase.execute(clubId, userId);
        return ResponseEntity.noContent().build();
    }

    // ── Membership management ──────────────────────────────────────────────

    @GetMapping("/clubs/{clubId}/members")
    public List<ClubMembership> listMembers(@PathVariable UUID clubId) {
        return membershipRepository.findByBookClub_ClubId(clubId);
    }

    @PutMapping("/clubs/{clubId}/members/{targetUserId}/role")
    public ResponseEntity<Void> changeMemberRole(
        @PathVariable UUID clubId,
        @PathVariable UUID targetUserId,
        @RequestHeader("X-User-Id") UUID requesterId,
        @RequestBody ChangeRoleRequest request
    ) {
        changeMemberRoleUseCase.execute(clubId, requesterId, targetUserId, request.role());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/clubs/{clubId}/members/{targetUserId}")
    public ResponseEntity<Void> removeMember(
        @PathVariable UUID clubId,
        @PathVariable UUID targetUserId,
        @RequestHeader("X-User-Id") UUID requesterId
    ) {
        removeMemberUseCase.execute(clubId, requesterId, targetUserId);
        return ResponseEntity.noContent().build();
    }

    // ── Messages ───────────────────────────────────────────────────────────

    @PostMapping("/clubs/{clubId}/messages")
    public Message postMessage(
        @PathVariable UUID clubId,
        @RequestHeader("X-User-Id") UUID userId,
        @RequestBody PostMessageRequest request
    ) {
        return postMessageUseCase.execute(clubId, userId, request.content());
    }

    @GetMapping("/clubs/{clubId}/messages")
    public Page<Message> listMessages(
        @PathVariable UUID clubId,
        Pageable pageable
    ) {
        return messageRepository.findByClubIdAndDeletedAtIsNull(clubId, pageable);
    }

    @PutMapping("/clubs/{clubId}/messages/{messageId}")
    public Message editMessage(
        @PathVariable UUID clubId,
        @PathVariable UUID messageId,
        @RequestHeader("X-User-Id") UUID userId,
        @RequestBody PostMessageRequest request
    ) {
        return editMessageUseCase.execute(messageId, userId, request.content());
    }

    @DeleteMapping("/clubs/{clubId}/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(
        @PathVariable UUID clubId,
        @PathVariable UUID messageId,
        @RequestHeader("X-User-Id") UUID userId
    ) {
        deleteMessageUseCase.execute(messageId, userId);
        return ResponseEntity.noContent().build();
    }

    // ── Request records ────────────────────────────────────────────────────

    record CreateClubRequest(String name, String description) {}
    record ChangeRoleRequest(ClubRole role) {}
    record PostMessageRequest(String content) {}
}
