package org.example.exception.security;

public class BlockedRequestException extends RuntimeException {

    public BlockedRequestException(String errorMessage) {
        super(errorMessage);
    }
}
