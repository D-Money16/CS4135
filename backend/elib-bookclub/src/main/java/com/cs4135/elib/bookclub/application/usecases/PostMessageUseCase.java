package com.cs4135.elib.bookclub.application.usecases;

import com.cs4135.elib.bookclub.domain.BookClub;
import com.cs4135.elib.bookclub.domain.Message;
import com.cs4135.elib.bookclub.dto.MessagePostedEvent;
import com.cs4135.elib.bookclub.infrastructure.BookClubRepository;
import com.cs4135.elib.bookclub.infrastructure.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PostMessageUseCase {

    @Autowired
    private BookClubRepository bookClubRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Transactional
    public Message execute(UUID clubId, UUID authorUserId, String content) {
        BookClub club = bookClubRepository.findById(clubId)
            .orElseThrow(() -> new IllegalArgumentException("Book club not found: " + clubId));

        // Validates membership — throws if user is not a member
        club.findMembership(authorUserId);

        Message message = new Message(clubId, authorUserId, content);
        Message saved = messageRepository.save(message);

        eventPublisher.publishEvent(
            new MessagePostedEvent(saved.getMessageId(), clubId, authorUserId)
        );

        return saved;
    }
}
