package com.mp.commons.exception;

/**
 * @author 刘瑞麒
 * @date 2026/11/6 15:09
 */
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
