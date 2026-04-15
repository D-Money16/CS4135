package com.cs4135.elib.bookclub.application.usecases;

import com.cs4135.elib.bookclub.domain.BookClub;
import com.cs4135.elib.bookclub.domain.ClubMembership;
import com.cs4135.elib.bookclub.domain.Message;
import com.cs4135.elib.bookclub.domain.MessageService;
import com.cs4135.elib.bookclub.infrastructure.BookClubRepository;
import com.cs4135.elib.bookclub.infrastructure.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DeleteMessageUseCase {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private BookClubRepository bookClubRepository;

    @Autowired
    private MessageService messageService;

    @Transactional
    public void execute(UUID messageId, UUID requesterId) {
        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new IllegalArgumentException("Message not found: " + messageId));

        BookClub club = bookClubRepository.findById(message.getClubId())
            .orElseThrow(() -> new IllegalArgumentException("Book club not found: " + message.getClubId()));

        ClubMembership membership = club.findMembership(requesterId);

        messageService.deleteMessage(message, requesterId, membership.getRole());
        messageRepository.save(message);
    }
}
