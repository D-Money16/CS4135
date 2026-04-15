package com.cs4135.elib.bookclub.domain;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MessageService {

    /**
     * Edits the content of a message. Only the original author may edit.
     * Sets editedAt timestamp on the message.
     */
    public void editMessage(Message message, String newContent, UUID requesterId) {
        if (message.isDeleted()) {
            throw new IllegalStateException("Cannot edit a deleted message");
        }
        message.edit(newContent, requesterId);
    }

    /**
     * Soft-deletes a message. The author or any MODERATOR/OWNER may delete.
     * Sets deletedAt timestamp; the record is retained in the database.
     */
    public void deleteMessage(Message message, UUID requesterId, ClubRole requesterRole) {
        if (message.isDeleted()) {
            throw new IllegalStateException("Message is already deleted");
        }
        message.softDelete(requesterId, requesterRole);
    }
}
