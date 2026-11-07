package com.mp.security.realm;

import com.alibaba.fastjson2.JSON;
import com.mp.commons.UserHolder;
import com.mp.commons.exception.InvalidTokenException;
import com.mp.commons.exception.JwtParseFairException;
import com.mp.commons.exception.UserAccountDisabled;
import com.mp.commons.util.JwtUtil;
import com.mp.pojo.UserInfo;
import com.mp.commons.properties.JwtProperty;
import com.mp.redis.connector.StringDataRedisConnector;
import com.mp.security.token.JwtToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static com.mp.redis.constant.RedisKeyConStant.LOGIN_USERINFO_USERID;

/**
 * @author 刘瑞麒
 * @date 2026/11/2 20:01
 */
@Component
@Slf4j
public class UserRealm extends AuthorizingRealm {
    @Resource
    private JwtProperty jwtProperty;
    /**
     *重写supports方法，支持解析JwtToken
     */
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JwtToken;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws RuntimeException {
        JwtToken jwtToken = (JwtToken) token;
        String accessToken = jwtToken.getAccessToken();
        ///校验accessToken
        Long userId;
        try {
            Claims claims = JwtUtil.parseToken(accessToken, jwtProperty.getSecretName());
            userId = Long.valueOf(claims.get("userId").toString());
        }catch (ExpiredJwtException e) {
            throw new JwtParseFairException("accessJwt合法但过期");
        }catch (JwtException e) {
            throw new InvalidTokenException("jwt校验失败");
        }
        /*
         * 根据userId查询redis，获取用户信息
         */
//        StringDataRedisConnector.opsForHash().get
        String key = LOGIN_USERINFO_USERID+userId;
        String id = StringDataRedisConnector.opsForHash().get(key, UserInfo.Fields.id);
        String username = StringDataRedisConnector.opsForHash().get(key, UserInfo.Fields.username);
        String status = StringDataRedisConnector.opsForHash().get(key, UserInfo.Fields.status);
        if ("0".equals(status)) {
            throw new UserAccountDisabled("账号被封禁");
        }
        ///TODO 从redis查询用户角色列表和权限列表，并封装到UserInfo对象
        String jsonRoles = StringDataRedisConnector.opsForHash().get(key, UserInfo.Fields.roles);
        String jsonPermission = StringDataRedisConnector.opsForHash().get(key, UserInfo.Fields.permissions);
        Set<String> roles = new HashSet<>(Objects.requireNonNull(JSON.parseArray(jsonRoles, String.class)));
        Set<String> permissions = new HashSet<>(Objects.requireNonNull(JSON.parseArray(jsonPermission, String.class)));
        UserInfo userInfo = UserInfo.builder()
                .id(Long.valueOf(id))
                .status(Integer.valueOf(status))
                .username(username)
                .roles(roles)
                .permissions(permissions)
                .build();
        String jwtStr = jwtToken.getAccessToken();
        ///将用户信息存入UserHolder
        UserHolder.setUser(userInfo);
        log.info("将用户信息存入UserHolder："+userInfo);
        return new SimpleAuthenticationInfo(
                userInfo,
                jwtStr,
                this.getName()
        );
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        UserInfo userInfo = (UserInfo) principals.getPrimaryPrincipal();
        SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
        Set<String> roles = userInfo.getRoles();
        Set<String> permissions = userInfo.getPermissions();
        simpleAuthorizationInfo.setRoles(roles);
        simpleAuthorizationInfo.setStringPermissions(permissions);
        return simpleAuthorizationInfo;
    }
}
