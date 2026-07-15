package com.robinhood.marketdata.exception;

public class InvalidMarketDataRequestException extends RuntimeException {
    public InvalidMarketDataRequestException(String message) {
        super(message);
    }
}
