package com.cs4135.elib.bookclub.infrastructure;

import com.cs4135.elib.bookclub.domain.BookClub;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BookClubRepository extends JpaRepository<BookClub, UUID> {
    // findById, findAll, save, delete inherited from JpaRepository
}
