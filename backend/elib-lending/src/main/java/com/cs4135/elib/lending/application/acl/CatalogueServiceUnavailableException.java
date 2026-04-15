package com.cs4135.elib.lending.application.acl;

public class CatalogueServiceUnavailableException extends RuntimeException {
    public CatalogueServiceUnavailableException(String message) {
        super(message);
    }
}
