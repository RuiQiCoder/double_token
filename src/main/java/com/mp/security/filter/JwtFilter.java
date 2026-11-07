package com.mp.security.filter;

import com.mp.commons.UserHolder;
import com.mp.commons.exception.*;
import com.mp.mapper.UserMapper;
import com.mp.pojo.UserInfo;
import com.mp.commons.properties.JwtProperty;
import com.mp.security.token.JwtToken;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * @author 刘瑞麒
 * @since  2026/11/2 19:38
 */
@Slf4j
public class JwtFilter extends AuthenticatingFilter {

    @Setter
    private JwtProperty jwtProperty;

    @Setter
    private UserMapper userMapper;

    /**
     * 当 Filter 中有了 handlerExceptionResolver，就可以把自己捕获到的异常，交给 Spring MVC 的异常处理体系处理
     */
    @Setter
    private HandlerExceptionResolver resolver;

    @Override
    protected AuthenticationToken createToken(ServletRequest servletRequest, ServletResponse servletResponse) {
        HttpServletRequest httpServletRequest = WebUtils.toHttp(servletRequest);
        //获取accessToken
        String bearer_accessToken = httpServletRequest.getHeader(jwtProperty.getTokenName()); // bearer .......
        if (StringUtils.isEmpty(bearer_accessToken)) {
            //用户未登录
            throw new LoseAccessTokenException("用户未登录");
        }
        if (!bearer_accessToken.startsWith("Bearer ") || bearer_accessToken.length() <= 7) {
            throw new InvalidTokenException("无效的 Authorization 请求头");
        }
        String accessToken = bearer_accessToken.substring(7);
        JwtToken jwtToken = new JwtToken();
//            jwtToken.setId(userId);
        jwtToken.setAccessToken(accessToken);
        return jwtToken;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        HttpServletRequest httpServletRequest = WebUtils.toHttp(servletRequest);
        log.info("onAccessDenied方法获取当前url："+httpServletRequest.getRequestURI());  //当前url
        HttpServletResponse httpServletResponse = WebUtils.toHttp(servletResponse);
        try {
            Subject subject = SecurityUtils.getSubject();
            JwtToken token = (JwtToken) createToken(servletRequest, servletResponse);
            subject.login(token);
        } catch (LoseAccessTokenException | JwtException | UserAccountDisabled | JwtParseFairException | UnknownAccountException | InvalidTokenException e) {
            resolver.resolveException(httpServletRequest, httpServletResponse, null, e);
            return false;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return true;
    }

    /**
     *只要请求经过JwtFilter，就一定会执行afterCompletion方法
     * 主要作用：请求结束时（不管有没有成功），是释放ThreadLocal缓存的User对象，避免内存泄漏
     */
    @Override
    public void afterCompletion(ServletRequest request, ServletResponse response, Exception exception) throws Exception{
        UserInfo userInfo = UserHolder.getUser();
        UserHolder.removeUser();
        log.info("释放ThreadLocal缓存的UserInfo对象："+userInfo);
        super.afterCompletion(request, response, exception);
    }
}
