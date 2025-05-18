package com.ttn.nexuscart.exceptions;

public class UnauthorisedAccessException extends RuntimeException{
    public UnauthorisedAccessException(String message) {
        super(message);
    }
}
