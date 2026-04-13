package com.cs4135.elib.catalogue.dto;

import com.cs4135.elib.catalogue.domain.AvailabilityStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class CopyAvailabilityChangedEvent {
    private UUID copyId;
    private UUID bookId;
    private AvailabilityStatus newStatus;
}
