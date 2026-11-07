package com.mp.redis.connector;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author 刘瑞麒
 * @date 2026/11/3 21:43
 */
public class RedisConnector {

    @Setter
    @Getter
    private static RedisTemplate<String, Object> redisTemplate;
}
