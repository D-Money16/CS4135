package com.cs4135.elib.catalogue.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class BookAddedEvent {
    private UUID bookId;
    private String isbn;
    private String title;
}
