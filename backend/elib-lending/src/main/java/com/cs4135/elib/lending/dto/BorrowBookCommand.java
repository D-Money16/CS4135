package com.cs4135.elib.lending.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class BorrowBookCommand {
    private UUID userId;
    private UUID bookId;
}
