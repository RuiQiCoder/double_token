package com.mp.commons.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * @author 刘瑞麒
 * @date 2026/11/2 18:09
 */
@Component
@ConfigurationProperties(prefix = "app.jwt")
@Data
public class JwtProperty {
    private String secretName;
    private String tokenName;
    private Duration accessTtl;
    private Duration refreshTtl;
}
