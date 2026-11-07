package com.mp.commons.exception.exceptionhandler;

import com.mp.commons.constant.MessageConstant;
import com.mp.commons.exception.BusinessException;
import com.mp.commons.exception.UserAccountDisabled;
import com.mp.commons.exception.UserAlreadyExistsException;
import com.mp.commons.result.Result;
import com.mp.commons.result.ResultCode;
import org.apache.shiro.authc.UnknownAccountException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;
import org.apache.shiro.authz.AuthorizationException;

/**
 * @author 刘瑞麒
 * @date 2026/11/5 09:03
 */
//决定匹配：@ExceptionHandler
@RestControllerAdvice
public class GlobalExceptionHandler {
//    @ExceptionHandler(Exception.class)表示：这个方法负责处理 Exception 及其子类异常。
//    如果同时有多个处理方法，在同一个处理类中通常会选择匹配更具体的那个
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Object> handleException(Exception e) {
        return Result.error(ResultCode.ERROR.getCode(), MessageConstant.SYSTEM_ERROR);
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleBusiness(Exception e) {
        return Result.error(ResultCode.BAD_REQUEST.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handlerMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        // 获取第一个校验失败的字段的错误信息
        FieldError fieldError = e.getBindingResult().getFieldError();

        // 提取你在 DTO 注解里写的 message，例如 "用户名不能为空"
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数校验失败";
        return Result.error(ResultCode.BAD_REQUEST.getCode(), message);
    }

    @ExceptionHandler(UnknownAccountException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Object> handlerUnknownAccountException(Exception e) {
        return Result.error(ResultCode.UNAUTHORIZED.getCode(), MessageConstant.USER_ACCOUNT_NOT_EXIST );
    }

    @ExceptionHandler(UserAccountDisabled.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Object> handlerUserAccountDisabled(Exception e) {
        return Result.error(ResultCode.FORBIDDEN.getCode(), MessageConstant.ACCOUNTLOCK);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Result<Object> handlerUserAlreadyExistsException(Exception e) {
        return Result.error(ResultCode.CONFLICT.getCode(), MessageConstant.USERARRDYEXISTINMYSQL);
    }

    @ExceptionHandler(AuthorizationException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Object> handleAuthorization(AuthorizationException e) {
        return Result.error(ResultCode.FORBIDDEN.getCode(), "无权限访问");
    }
}
