package com.mp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author 刘瑞麒
 * @date 2026/11/2 14:21
 */
@Data
public class LoginDTO {
    @NotBlank(message = "用户名不能为空")   //只能用于字符串
    @Pattern(regexp = "^[a-zA-Z0-9_]{4,20}$", message = "用户名必须是4~20位字母和数字组成的字符串")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "\\A(?=.*[0-9])(?=.*[A-Za-z])(?=.*[\\x21-\\x2F\\x3A-\\x40\\x5B-\\x60\\x7B-\\x7E])[\\x21-\\x7E]{6,26}\\z", message = "密码必须是数字、字母、特殊字符组成的6~26位字符串")
    private String password;
    /**
     * 注册or登录（0：注册，1：登录
     */
    @NotBlank(message = "系统异常")
    @Pattern(regexp = "^[01]$", message = "系统异常")
    private String isLogin;
}
