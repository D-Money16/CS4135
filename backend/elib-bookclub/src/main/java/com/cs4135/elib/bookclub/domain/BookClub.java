package com.cs4135.elib.bookclub.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "book_clubs")
@Getter
@Setter
@NoArgsConstructor
public class BookClub {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "club_id")
    private UUID clubId;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "bookClub", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonIgnore
    private List<ClubMembership> memberships = new ArrayList<>();

    public BookClub(String name, String description) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Book club name cannot be empty");
        }
        this.name = name;
        this.description = description;
        this.createdAt = LocalDateTime.now();
    }

    public ClubMembership addMember(UUID userId, ClubRole role) {
        boolean alreadyMember = memberships.stream()
            .anyMatch(m -> m.getUserId().equals(userId));
        if (alreadyMember) {
            throw new IllegalStateException("User " + userId + " is already a member of this club");
        }
        ClubMembership membership = new ClubMembership(this, userId, role);
        memberships.add(membership);
        return membership;
    }

    public void removeMember(UUID userId) {
        ClubMembership membership = findMembership(userId);
        if (membership.getRole() == ClubRole.OWNER) {
            throw new IllegalStateException("Cannot remove the OWNER. Transfer ownership first.");
        }
        memberships.remove(membership);
    }

    public ClubMembership findMembership(UUID userId) {
        return memberships.stream()
            .filter(m -> m.getUserId().equals(userId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("User " + userId + " is not a member of this club"));
    }

    public boolean isMember(UUID userId) {
        return memberships.stream().anyMatch(m -> m.getUserId().equals(userId));
    }
}
