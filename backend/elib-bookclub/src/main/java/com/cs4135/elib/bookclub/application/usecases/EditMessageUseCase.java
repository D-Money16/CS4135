package com.cs4135.elib.bookclub.application.usecases;

import com.cs4135.elib.bookclub.domain.Message;
import com.cs4135.elib.bookclub.domain.MessageService;
import com.cs4135.elib.bookclub.infrastructure.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class EditMessageUseCase {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private MessageService messageService;

    @Transactional
    public Message execute(UUID messageId, UUID requesterId, String newContent) {
        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new IllegalArgumentException("Message not found: " + messageId));

        messageService.editMessage(message, newContent, requesterId);

        return messageRepository.save(message);
    }
}
