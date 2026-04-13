package com.cs4135.elib.catalogue.application.acl;

import com.cs4135.elib.catalogue.application.usecases.CheckAvailabilityUseCase;
import com.cs4135.elib.catalogue.application.usecases.ReleaseBookCopyUseCase;
import com.cs4135.elib.catalogue.application.usecases.ReserveBookCopyUseCase;
import com.cs4135.elib.catalogue.dto.CatalogueAvailabilityResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CatalogueACL {

    @Autowired
    private ReserveBookCopyUseCase reserveBookCopyUseCase;

    @Autowired
    private ReleaseBookCopyUseCase releaseBookCopyUseCase;

    @Autowired
    private CheckAvailabilityUseCase checkAvailabilityUseCase;

    public UUID reserveAvailableCopy(UUID bookId) {
        return reserveBookCopyUseCase.execute(bookId);
    }

    public void releaseCopy(UUID copyId) {
        releaseBookCopyUseCase.execute(copyId);
    }

    public CatalogueAvailabilityResponse checkCopyAvailability(UUID copyId) {
        return checkAvailabilityUseCase.executeForCopy(copyId);
    }
}
