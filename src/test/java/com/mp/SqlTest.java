package com.mp;

import com.mp.mapper.UserMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

/**
 * @author 刘瑞麒
 * @date 2026/11/7 17:05
 */
@SpringBootTest
@Slf4j
public class SqlTest {
    @Resource
    private UserMapper userMapper;

    @Test
    public void test() {
        Set<String> roles = userMapper.getRoles(15L);
        Set<String> permissions = userMapper.getPermissions((15L));
        log.info("用户角色列表：");
        roles.forEach(System.out::println);
        log.info("用户权限列表：");
        permissions.forEach(System.out::println);
    }
}
