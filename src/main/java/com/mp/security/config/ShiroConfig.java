package com.mp.security.config;

import com.mp.commons.properties.PathProperty;
import com.mp.mapper.UserMapper;
import com.mp.commons.properties.JwtProperty;
import com.mp.security.filter.JwtFilter;
import jakarta.annotation.Resource;
import org.apache.shiro.authz.Authorizer;
import org.apache.shiro.authz.ModularRealmAuthorizer;
import org.apache.shiro.mgt.DefaultSessionStorageEvaluator;
import org.apache.shiro.mgt.SessionStorageEvaluator;
import org.apache.shiro.spring.web.config.DefaultShiroFilterChainDefinition;
import org.apache.shiro.spring.web.config.ShiroFilterChainDefinition;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class ShiroConfig {
    @Resource
    private PathProperty pathProperty;
    // Realm 也实现了 Authorizer，需显式提供 Starter 按名称查找的授权器。
    @Bean
    public Authorizer authorizer() {
        return new ModularRealmAuthorizer();
    }

    // 1. 配置 URL 使用哪些 Shiro 过滤器
    @Bean
    public ShiroFilterChainDefinition shiroFilterChainDefinition() {
        DefaultShiroFilterChainDefinition chain =
                new DefaultShiroFilterChainDefinition();
        /// (接口路径, 拦截器名称)
        Map<String, String> anonMap = pathProperty.getAnonPath().stream()
                        .collect(Collectors.toMap( Function.identity(), e->pathProperty.getAnon()));
        Map<String, String> jwtMap = pathProperty.getJwtPath().stream()
                        .collect(Collectors.toMap(Function.identity(), e->pathProperty.getJwt()));
        chain.addPathDefinitions(anonMap);
        chain.addPathDefinitions(jwtMap);
        return chain;
    }

    // 3. 禁止将 Subject 的认证状态存入 Session
    @Bean
    public SessionStorageEvaluator sessionStorageEvaluator() {
        DefaultSessionStorageEvaluator evaluator =
                new DefaultSessionStorageEvaluator();

        evaluator.setSessionStorageEnabled(false);
        return evaluator;
    }

    //4. 定制JwtFilter
    @Bean("jwt") // 必须与过滤链中的名称一致，Starter 按 Bean 名称注册过滤器。
    public JwtFilter jwtFilter(JwtProperty jwtProperty, UserMapper userMapper, @Lazy HandlerExceptionResolver handlerExceptionResolver) {
        JwtFilter jwtFilter = new JwtFilter();
        jwtFilter.setJwtProperty(jwtProperty);
        jwtFilter.setResolver(handlerExceptionResolver);
        jwtFilter.setUserMapper(userMapper);
        return jwtFilter;
    }

    // 2. 禁止 JwtFilter 被 Servlet 容器独立执行
    @Bean
    public FilterRegistrationBean<JwtFilter> jwtFilterRegistration(
            JwtFilter jwtFilter) {

        FilterRegistrationBean<JwtFilter> registration =
                new FilterRegistrationBean<>(jwtFilter);

        registration.setEnabled(false);
        return registration;
    }
}
