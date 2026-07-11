package com.robinhood.user.exception;

// Runtime exceptions don't require try/catch at call sites.
// Spring's @ExceptionHandler will catch this and return a 409 response.
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
