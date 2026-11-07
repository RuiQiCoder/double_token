package com.mp.commons.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Objects;

public final class RefreshTokenUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    private RefreshTokenUtil() {
    }

    // 生成 32 字节随机数，即 256 位随机数据
    public static String generate() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    // 计算 Token 的 SHA-256 哈希，供后端存储和比对
    public static String hashSHA256 (String token) {
        Objects.requireNonNull(token, "refresh token不能为空");

        if (token.isBlank()) {
            throw new IllegalArgumentException("refresh token不能为空");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("当前运行环境不支持SHA-256", e);
        }
    }
}