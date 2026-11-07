package com.mp;

import com.mp.redis.connector.StringDataRedisConnector;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ListOperations;

/**
 * @author 刘瑞麒
 * @date 2026/11/6 23:23
 */
@SpringBootTest
@Slf4j
public class RedisTest {
    @Test
    public void hashToObj() {
        String id = StringDataRedisConnector.opsForHash().get("login:userinfo:15", "id");
        String username = StringDataRedisConnector.opsForHash().get("login:userinfo:15", "username");
        String status = StringDataRedisConnector.opsForHash().get("login:userinfo:15", "status");
        log.info("id："+id);
        log.info("username："+username);
        log.info("status："+status);
    }

    @Test
    public void StringDataRedisForList() {
        ListOperations<String, String> list =
                StringDataRedisConnector.opsForList();

        String key = "demo:list";
    }
}
