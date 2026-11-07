package com.mp.security.exception;

import com.mp.commons.constant.MessageConstant;
import com.mp.commons.exception.*;
import com.mp.commons.result.Result;
import com.mp.commons.result.ResultCode;
import io.jsonwebtoken.JwtException;
import org.apache.shiro.authc.UnknownAccountException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author 刘瑞麒
 * @date 2026/11/5 10:42
 */
@Order(0)
@RestControllerAdvice
public class JwtFilterExceptionHandler {
    /**
     * AccessToken缺失异常
     */
    @ExceptionHandler(LoseAccessTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Object> handleLoseAccessTokenException(LoseAccessTokenException loseAccessTokenException) {
        return Result.error(ResultCode.UNAUTHORIZED.getCode(), MessageConstant.USERNOTLOGIN);
    }
    /**
     * jwt其它解析异常
     */
    @ExceptionHandler(JwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Object> handleJwtParseFairException(JwtException jwtException) {
        return Result.error(ResultCode.UNAUTHORIZED.getCode(), "令牌无效或已过期");
    }

    @ExceptionHandler(UserAccountDisabled.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Object> handleUserAccountDisabled(Exception e) {
        return Result.error(ResultCode.FORBIDDEN.getCode(), MessageConstant.ACCOUNTLOCK);
    }

    @ExceptionHandler(JwtParseFairException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Object> handleJwtParseFairException(Exception e) {
        return Result.error(ResultCode.UNAUTHORIZED.getCode(), MessageConstant.JWTTIMEOUT );
    }

    @ExceptionHandler(UnknownAccountException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Object> handleUnknownAccountException(Exception e) {
        return Result.error(ResultCode.UNAUTHORIZED.getCode(), MessageConstant.USERNOTLOGIN);
    }

    @ExceptionHandler(InvalidTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Object> handleInvalidTokenException(Exception e) {
        return Result.error(ResultCode.UNAUTHORIZED.getCode(), MessageConstant.JWTInvalidDISABLED);
    }
}
