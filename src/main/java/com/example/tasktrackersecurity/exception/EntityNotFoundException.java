package com.example.tasktrackersecurity.exception;

public class EntityNotFoundException extends RuntimeException{

    public EntityNotFoundException(String errorMessage) {
        super(errorMessage);
    }
}
