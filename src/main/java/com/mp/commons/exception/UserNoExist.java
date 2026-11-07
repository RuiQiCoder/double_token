package com.mp.commons.exception;

/**
 * @author 刘瑞麒
 * @date 2026/11/5 13:06
 */
public class UserNoExist extends RuntimeException {
    public UserNoExist(String message) {
        super(message);
    }
}
