package com.mp;

import com.mp.commons.result.ResultCode;
import org.junit.jupiter.api.Test;

/**
 * @author 刘瑞麒
 * @date 2026/11/5 12:22
 */
public class EnumTest {
    @Test
    public void t() {
        System.out.println(ResultCode.ERROR.getCode()+"："+ResultCode.ERROR.getMessage());
    }
}
