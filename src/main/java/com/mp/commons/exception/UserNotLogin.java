package com.mp.commons.exception;

/**
 * @author 刘瑞麒
 * @date 2026/11/6 17:58
 */
public class UserNotLogin extends RuntimeException {
    public UserNotLogin(String message) {
        super(message);
    }
}
