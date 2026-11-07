package com.mp.security.token;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.shiro.authc.AuthenticationToken;

/**
 * @author 刘瑞麒
 * @date 2026/11/2 18:54
 */

/**
 *token 是外部传进来的，`id` 通常从校验通过的 JWT 中解析出来。 JwtToken 本身不会自动生成这些数据。
 */
public class JwtToken implements AuthenticationToken {

    @Getter
    @Setter
    public Long id;

    @Getter
    @Setter
    public String accessToken;

    @Override
    public Object getPrincipal() {
        return this.id;
    }

    @Override
    public Object getCredentials() {
        return this.accessToken;
    }

}
