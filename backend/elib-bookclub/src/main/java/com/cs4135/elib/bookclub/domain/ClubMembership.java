package com.cs4135.elib.bookclub.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "club_memberships",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_membership_club_user",
        columnNames = {"club_id", "user_id"}
    )
)
@Getter
@Setter
@NoArgsConstructor
public class ClubMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "membership_id")
    private UUID membershipId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    @JsonIgnore
    private BookClub bookClub;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClubRole role;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    public ClubMembership(BookClub bookClub, UUID userId, ClubRole role) {
        this.bookClub = bookClub;
        this.userId = userId;
        this.role = role;
        this.joinedAt = LocalDateTime.now();
    }

    @JsonProperty("clubId")
    public UUID getClubId() {
        return bookClub != null ? bookClub.getClubId() : null;
    }
}
