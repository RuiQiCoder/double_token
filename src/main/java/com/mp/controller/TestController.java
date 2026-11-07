package com.mp.controller;

import com.mp.commons.UserHolder;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 刘瑞麒
 * @date 2026/11/2 20:45
 */
@RestController
@RequestMapping("/test")
public class TestController {
    /**
     * t1不需要认证
     */
    @GetMapping("/t1")
    public String t1() {
       return "t1";
    }

    /**
     * t2需要认证
     */

    ///AuthorizationInfo 提供权限数据，`@RequiresPermissions` 触发校验，Shiro 执行判断
    @GetMapping("/t2")
    @RequiresPermissions("profile:read")
    public String t2() {
        return "t2，我是："+ UserHolder.getUser().getUsername();
    }
}
