package com.project.hearmeout_backend.common_lib.exception;

public class PostNotFoundException extends RuntimeException{
    public PostNotFoundException(String message){
        super(message);
    }
}
