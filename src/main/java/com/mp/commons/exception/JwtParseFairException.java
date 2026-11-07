package com.mp.commons.exception;

import org.apache.shiro.authc.AuthenticationException;

/**
 * @author 刘瑞麒
 * @date 2026/11/5 13:20
 */
public class JwtParseFairException extends AuthenticationException  {
    public JwtParseFairException(String message) {
        super(message);
    }
}
