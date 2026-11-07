package com.mp.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author 刘瑞麒
 * @date 2026/11/7 19:14
 */
@Data
@TableName("sys_user_role")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User_Role {
    private Long userId;
    private Long roleId;
    private LocalDateTime createdAt;
}
