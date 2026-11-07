package com.mp.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 刘瑞麒
 * @date 2026/11/4 20:16
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenVO {
    private String accessToken;
    private String refreshToken;
}
