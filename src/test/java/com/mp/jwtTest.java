package com.mp;

import com.mp.commons.util.JwtUtil;
import com.mp.commons.properties.JwtProperty;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author 刘瑞麒
 * @date 2026/11/5 13:43
 */
@SpringBootTest
@Slf4j
public class jwtTest {
    @Resource
    private JwtProperty jwtProperty;

    @Test
    public void createJwt() {
        log.info("jwt："+ JwtUtil.createToken(4L, jwtProperty.getSecretName()), jwtProperty.getAccessTtl());
    }
}
