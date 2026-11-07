package com.mp.commons.exception;

/**
 * @author 刘瑞麒
 * @date 2026/11/5 09:17
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
