package com.mp.commons.exception;

import org.apache.shiro.authc.AuthenticationException;

/**
 * @author 刘瑞麒
 * @date 2026/11/5 10:30
 */
public class LoseAccessTokenException extends AuthenticationException {
    public LoseAccessTokenException(String message) {
        super(message);
    }
}
