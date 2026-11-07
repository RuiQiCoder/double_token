package com.mp.commons.exception;

import org.apache.shiro.authc.AuthenticationException;

/**
 * @author 刘瑞麒
 * @date 2026/11/5 13:05
 */
public class UserAccountDisabled extends AuthenticationException {
    public UserAccountDisabled(String message) {
        super(message);
    }
}
