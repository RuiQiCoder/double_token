package com.mp.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mp.commons.constant.MessageConstant;
import com.mp.commons.exception.UserAccountDisabled;
import com.mp.commons.exception.UserAlreadyExistsException;
import com.mp.commons.result.Result;
import com.mp.commons.result.ResultCode;
import com.mp.commons.util.JwtUtil;
import com.mp.commons.util.RefreshTokenUtil;
import com.mp.dto.LoginDTO;
import com.mp.mapper.UserMapper;
import com.mp.pojo.User;
import com.mp.pojo.UserInfo;
import com.mp.commons.properties.JwtProperty;
import com.mp.pojo.User_Role;
import com.mp.redis.connector.StringDataRedisConnector;
import com.mp.redis.constant.RedisKeyConStant;
import com.mp.service.UserRoleService;
import com.mp.service.UserService;
import com.mp.vo.LoginVO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.UnknownAccountException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author 刘瑞麒
 * @date 2026/11/2 14:12
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Resource
    private JwtProperty jwtProperty;
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserRoleService userRoleService;

    @Transactional
    @Override
    public Result<LoginVO> login(LoginDTO loginDTO, HttpServletResponse response) {
        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();
        String isLogin = loginDTO.getIsLogin();
        /**
         *  1. 校验用户名密码的格式，校验isLogin是不是1 or 2
         */
        if ("0".equals(isLogin)) {
            ///2. 如果是注册
            String hash = BCrypt.hashpw(password, BCrypt.gensalt());
            User user = User.builder()
                    .username(username)
                    .passwordHash(hash)
                    .build();
            //将用户存入mysql
            try {
                save(user);
            } catch (DuplicateKeyException e) {
                log.warn("注册唯一键冲突: {}", e.getMessage());   // 细节只进日志
                throw new UserAlreadyExistsException("该用户已注册");
            }
            //为用户添加角色 -- user
            User_Role userRole = User_Role.builder()
                    .userId(user.getId())
                    .roleId(2L)    // 普通用户
                    .build();
            userRoleService.save(userRole);
            return Result.success(null);
        } else {
            ///3. 如果是登录
            /// 3.1 根据用户名查user表
            User user = getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
            if (Objects.isNull(user)) {
                throw new UnknownAccountException("账户不存在");
            }
            /// 3.2 检查密码
            boolean ok = BCrypt.checkpw(password, user.getPasswordHash());
            if (!ok) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return Result.error(ResultCode.UNAUTHORIZED.getCode(), MessageConstant.PASSWORD_FAIR);
            }
            ///3.3 检查用户账号是否封禁
            if (!Integer.valueOf(1).equals(user.getStatus())) {
                throw new UserAccountDisabled("账号锁定");
            }
            ///4. 登录成功，将UserInfo缓存到redis，并生成jwt
            /**
             * 常见设计有两种：
             * 1. 登录返回 token + userInfo，前端直接显示用户界面。
             * 2！！！. 登录只返回 token，前端再携带 token 请求 /users/me 获取当前用户信息。
             */
            Long userId = user.getId();
            //查询用户角色列表
            Set<String> roles = getRoles(userId);
            //查询用户权限列表
            Set<String> permissions = getPermissions(userId);
            String key = "login:userinfo:"+userId;
            UserInfo userInfo = UserInfo.builder()
                    .id(userId)
                    .status(user.getStatus())
                    .username(user.getUsername())
                    .build();
            //将userInfo转为map，存入redis
            Map<String, String> userMap = new ObjectMapper().convertValue(userInfo, new TypeReference<>() {});
            userMap.put(UserInfo.Fields.roles, JSON.toJSONString(roles));
            userMap.put(UserInfo.Fields.permissions, JSON.toJSONString(permissions));
            userInfo.setRoles(roles);
            userInfo.setPermissions(permissions);
            StringDataRedisConnector.opsForHash().putAll(key, userMap);
            //生成accessToken
            String accessToken = JwtUtil.createToken(userId, jwtProperty.getSecretName(), jwtProperty.getAccessTtl());
            //生成refreshToken
            StringBuilder stringBuilder = new StringBuilder();
            String refreshToken = stringBuilder
                    .append(userId)
                    .append(".")
                    .append(RefreshTokenUtil.generate())
                    .toString();
            //生成refreshToken的哈希（SHA-256 仍被认为是抗碰撞的密码学哈希）
            String hash = RefreshTokenUtil.hashSHA256(refreshToken);
            //以login:refreshtoken:拼接userId为key，refreshtoken的SHA-256加密哈希为值，存入redis，并设置过期时间
            StringDataRedisConnector.opsForValue().set(RedisKeyConStant.LOGIN_REFRESHTOKEN_USERID+userId, hash, jwtProperty.getRefreshTtl());
            //ResponseCookie的作用，最终体现为 HTTP 响应头里的 Set-Cookie
            ResponseCookie cookie = ResponseCookie
                    .from("refreshToken", refreshToken)
                    .httpOnly(true)              // 禁止 JavaScript 读取
//                    .secure(true)                // 仅通过 HTTPS 发送
                    .sameSite("Lax")              // 限制跨站携带
                    .path("/user/login/refresh")               // /user/login/refresh 下的请求可以携带
                    .maxAge(jwtProperty.getRefreshTtl())    //有效期
                    .build();
            //往请求头添加cookie
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
            //返回accessToken
            LoginVO data = new LoginVO();
            data.setAccessToken(accessToken);
            return Result.success(data);
        }
    }

    @Transactional
    @Override
    public Result<LoginVO> refresh(String refreshToken, HttpServletResponse response) {
        if (refreshToken == null || !refreshToken.matches("[1-9][0-9]*\\.[A-Za-z0-9_-]{43}")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return Result.error(40102, "校验失败，要求重新登录");
        }
        String userId = refreshToken.split("\\.", 2)[0];
        //校验用户账号状态
        String statusRedis = StringDataRedisConnector.opsForHash().get(RedisKeyConStant.LOGIN_USERINFO_USERID + userId, "status");
        if (!"1".equals(statusRedis)) {
            removeRedis(userId);
            throw new UserAccountDisabled("账号不可用或会话已失效");
        }
        String hash = RefreshTokenUtil.hashSHA256(refreshToken);
        String hashRedis = StringDataRedisConnector.opsForValue().get(RedisKeyConStant.LOGIN_REFRESHTOKEN_USERID+userId);
        if (hash.equals(hashRedis)) {
            log.info("校验成功，生成新accessToken和refreshToken，并刷新redis用户登录的refreshToken的hash值，新 Refresh Token 写回 Cookie");
            getRefreshTokenAndSetCookie(userId, response);
            LoginVO loginVO = generateAccessTokenAndGetLoginVO(userId);
            return Result.success(loginVO);
        }else {
            log.info("校验失败，要求重新登录");
//            removeRedis(userId);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);  //httpCode：401
            //这里和前端约定：
            return Result.error(40102, "校验失败，要求重新登录");   //40102约定：refreshToken过期，下一次请求请访问/user/login接口
        }
    }

    @NonNull
    private LoginVO generateAccessTokenAndGetLoginVO(String userId) {
        String newAccessToken = JwtUtil.createToken(Long.valueOf(userId), jwtProperty.getSecretName(), jwtProperty.getAccessTtl());
        LoginVO loginVO = new LoginVO(newAccessToken);
        return loginVO;
    }

    /**
     * 生成新refreshToken，并将hash值存入redis(覆盖旧hash)
     */
    @NonNull
    private void getRefreshTokenAndSetCookie(String userId, HttpServletResponse response) {
        String newRefreshToken = userId +"."+RefreshTokenUtil.generate();
        String newHash = RefreshTokenUtil.hashSHA256(newRefreshToken);
        StringDataRedisConnector.opsForValue().set(RedisKeyConStant.LOGIN_REFRESHTOKEN_USERID+ userId, newHash, jwtProperty.getRefreshTtl());  //刷新redis用户登录的refreshToken的hash值，并刷新时间
        ResponseCookie cookie = ResponseCookie
                .from("refreshToken", newRefreshToken)
                .httpOnly(true)              // 禁止 JavaScript 读取
                // 本地 HTTP 联调与首次登录保持一致；生产 HTTPS 应统一启用 Secure。
                .sameSite("Lax")              // 限制跨站携带
                .path("/user/login/refresh")
                .maxAge(jwtProperty.getRefreshTtl())
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private static void removeRedis(String userId) {
        ///删除redis的用户refreshToken的哈希和用户基本信息
        StringDataRedisConnector.remove(RedisKeyConStant.LOGIN_USERINFO_USERID+ userId);
        StringDataRedisConnector.remove(RedisKeyConStant.LOGIN_REFRESHTOKEN_USERID+ userId);
    }

    private Set<String> getRoles(Long userId) {
        return userMapper.getRoles(userId);
    }

    private Set<String> getPermissions(Long userId) {
        return userMapper.getPermissions(userId);
    }
}
