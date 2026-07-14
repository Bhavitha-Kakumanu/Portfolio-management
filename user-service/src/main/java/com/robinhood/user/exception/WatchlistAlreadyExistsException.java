package com.robinhood.user.exception;

public class WatchlistAlreadyExistsException extends RuntimeException {
    public WatchlistAlreadyExistsException(String message) {
        super(message);
    }
}
