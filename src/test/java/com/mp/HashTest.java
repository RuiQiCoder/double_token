package com.mp;

import com.mp.commons.util.RefreshTokenUtil;

/**
 * @author 刘瑞麒
 * @date 2026/11/5 15:23
 */

public class HashTest {
    public static void main(String[] args) {
        String hash = RefreshTokenUtil.hashSHA256("15.srbjIcqBZSOgESgkVWyz7RmvwmEQPHLXfKTIzAeEtZY");
        System.out.println(hash);
    }
}
