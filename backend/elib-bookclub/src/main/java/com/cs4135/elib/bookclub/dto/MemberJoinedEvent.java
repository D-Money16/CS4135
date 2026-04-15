package com.cs4135.elib.bookclub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberJoinedEvent {
    private UUID clubId;
    private String clubName;
    private UUID userId;
    private LocalDateTime joinedAt;
}
