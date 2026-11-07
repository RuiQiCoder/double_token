package com.mp.controller;

import com.mp.commons.result.Result;
import com.mp.dto.LoginDTO;
import com.mp.service.UserService;
import com.mp.vo.LoginVO;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.web.bind.annotation.*;

/**
 * @author 刘瑞麒
 * @date 2026/11/2 11:03
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;
    /**
     * 登录注册接口（二合一）
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO, ServletResponse response) {
        return userService.login(loginDTO, WebUtils.toHttp(response));
    }

    /**
     * 刷新，签发refreshtoken的接口 (accessToken过期，请求到达这个接口，
     * 如果redis的refreshtoken没有过期，生成新的accessToken返回；
     * 如果refreshtoken过期，返回错误信息，然后请求到达登录接口，要求重新登录)
     */
    @PostMapping("/login/refresh")
    public Result<LoginVO> refresh(@CookieValue(value = "refreshToken", required = false) String refreshToken, ServletResponse response) {
        return userService.refresh(refreshToken, WebUtils.toHttp(response));
    }
}
