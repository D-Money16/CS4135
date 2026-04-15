package com.cs4135.elib.bookclub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookClubCreatedEvent {
    private UUID clubId;
    private String name;
    private UUID ownerId;
}
