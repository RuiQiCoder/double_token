package com.mp.commons.exception;

import org.apache.shiro.authc.AuthenticationException;

/**
 * @author 刘瑞麒
 * @date 2026/11/7 09:45
 */
public class InvalidTokenException extends AuthenticationException {
    public InvalidTokenException(String msg) { super(msg); }
}
