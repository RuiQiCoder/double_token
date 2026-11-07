package com.mp.commons.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * @author 刘瑞麒
 * @date 2026/11/7 16:15
 */
@ConfigurationProperties(prefix = "app.shiro")
@Component
@Data
public class PathProperty {
    private String anon = "anon";
    private String jwt = "jwt";
    private Set<String> anonPath;
    private Set<String> jwtPath;
}
