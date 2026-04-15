package com.cs4135.elib.bookclub.infrastructure;

import com.cs4135.elib.bookclub.domain.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    // Paginated — excludes soft-deleted messages
    Page<Message> findByClubIdAndDeletedAtIsNull(UUID clubId, Pageable pageable);
}
