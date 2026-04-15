package com.cs4135.elib.bookclub.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "message_id")
    private UUID messageId;

    @Embedded
    private MessageContent content;

    @Column(name = "club_id", nullable = false)
    private UUID clubId;

    @Column(name = "author_user_id", nullable = false)
    private UUID authorUserId;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @Column(name = "edited_at")
    private LocalDateTime editedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public Message(UUID clubId, UUID authorUserId, String content) {
        this.clubId = clubId;
        this.authorUserId = authorUserId;
        this.content = new MessageContent(content);
        this.sentAt = LocalDateTime.now();
    }

    public void edit(String newContent, UUID requesterId) {
        if (!authorUserId.equals(requesterId)) {
            throw new IllegalStateException("Only the author can edit this message");
        }
        this.content = new MessageContent(newContent);
        this.editedAt = LocalDateTime.now();
    }

    public void softDelete(UUID requesterId, ClubRole requesterRole) {
        boolean isAuthor = authorUserId.equals(requesterId);
        boolean isModerator = requesterRole == ClubRole.MODERATOR || requesterRole == ClubRole.OWNER;
        if (!isAuthor && !isModerator) {
            throw new IllegalStateException("Only the author or a moderator may delete this message");
        }
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
