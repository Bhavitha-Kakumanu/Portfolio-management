package com.robinhood.user.exception;

public class WatchlistNotFoundException extends RuntimeException {
    public WatchlistNotFoundException(String message) {
        super(message);
    }
}
