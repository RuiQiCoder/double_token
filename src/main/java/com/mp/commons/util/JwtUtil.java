package com.mp.commons.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Objects;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * @author 刘瑞麒
 * @date 2026/11/2 18:02
 */
public final class JwtUtil {

    public static final String USER_ID_CLAIM = "userId";
    private static final Duration DEFAULT_TTL = Duration.ofHours(2);

    private JwtUtil() {
    }

    /**
     * 生成 HS256 签名的 JWT，payload 包含 userId 和 exp，默认有效期为 2 小时。
     *
     * @param userId 用户主键
     * @param base64Secret Base64 编码的签名密钥，解码后至少 32 字节；生成和校验须使用同一密钥
     */
    public static String createToken(Long userId, String base64Secret) {
        return createToken(userId, base64Secret, DEFAULT_TTL);
    }

    /** 生成指定有效期的 JWT，ttl 必须大于零。 */
    public static String createToken(Long userId, String base64Secret, Duration ttl) {
        Objects.requireNonNull(userId, "userId不能为空");
        Objects.requireNonNull(ttl, "有效期不能为空");
        if (ttl.isZero() || ttl.isNegative()) {
            throw new IllegalArgumentException("有效期必须大于零");
        }
        return Jwts.builder()
                .claim(USER_ID_CLAIM, userId)
                .expiration(Date.from(Instant.now().plus(ttl)))
                .signWith(signingKey(base64Secret), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 验证签名并返回 Claims，调用方可通过 claims.get("userId", Long.class) 获取用户 ID。
     *
     * @throws ExpiredJwtException token 已过期
     * @throws JwtException token 为空、格式错误、签名无效或 claims 不符合要求
     * @throws IllegalArgumentException 签名密钥为空
     */
    public static Claims parseToken(String token, String base64Secret)
            throws ExpiredJwtException, JwtException {
        SecretKey key = signingKey(base64Secret);
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            if (claims.size() != 2 || claims.get(USER_ID_CLAIM, Long.class) == null
                    || claims.getExpiration() == null) {
                throw new MalformedJwtException("payload必须包含且仅包含userId和exp");
            }
            return claims;
        } catch (ExpiredJwtException e) {
            throw e;
        } catch (JwtException | IllegalArgumentException e) {
            throw new JwtException("JWT校验失败", e);
        }
    }

    private static SecretKey signingKey(String base64Secret) {
        if (base64Secret == null || base64Secret.isBlank()) {
            throw new IllegalArgumentException("签名密钥不能为空");
        }
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
    }
}
