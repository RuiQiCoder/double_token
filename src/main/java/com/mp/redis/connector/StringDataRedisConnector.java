package com.mp.redis.connector;

import lombok.Setter;
import org.springframework.data.redis.core.*;

/**
 * @author 刘瑞麒
 * @date 2026/11/3 14:29
 */
public class StringDataRedisConnector {
    @Setter
    private static StringRedisTemplate stringRedisTemplate;

    public static ValueOperations<String, String> opsForValue() {
        return stringRedisTemplate.opsForValue();
    }

    public static HashOperations<String, String, String> opsForHash() {
        return stringRedisTemplate.opsForHash();
    }

    public static ListOperations<String, String> opsForList() {
        return stringRedisTemplate.opsForList();
    }

    public static SetOperations<String, String> opsForSet() {
        return stringRedisTemplate.opsForSet();
    }

    public static ZSetOperations<String, String> opsForZSet() {
        return stringRedisTemplate.opsForZSet();
    }

    //删除
    public static boolean remove(String key) {
        return stringRedisTemplate.delete(key);
    }
}

