package com.jane.core.exceptions;

public class CorruptSaveFileException extends RuntimeException {
    public CorruptSaveFileException(String message) {
        super(message);
    }
}
