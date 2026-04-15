package com.cs4135.elib.bookclub.infrastructure;

import com.cs4135.elib.bookclub.domain.ClubMembership;
import com.cs4135.elib.bookclub.domain.ClubRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClubMembershipRepository extends JpaRepository<ClubMembership, UUID> {

    Optional<ClubMembership> findByBookClub_ClubIdAndUserId(UUID clubId, UUID userId);

    List<ClubMembership> findByBookClub_ClubId(UUID clubId);

    List<ClubMembership> findByBookClub_ClubIdAndRole(UUID clubId, ClubRole role);
}
