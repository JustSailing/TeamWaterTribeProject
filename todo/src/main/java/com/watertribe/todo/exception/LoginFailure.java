package com.watertribe.todo.exception;

public class LoginFailure extends RuntimeException {

    public LoginFailure(String message) {
        super(message);
    }
}
