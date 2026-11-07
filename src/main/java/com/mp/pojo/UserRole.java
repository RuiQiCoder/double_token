package com.mp.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author 刘瑞麒
 * @date 2026/11/2 13:36
 *
 * 用户--角色多对多关联表
 */
@TableName("sys_user_role")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRole {

    /**
     * 用户id，关联用户表
     */
    @TableField("user_id")
    private Long UserId;

    /**
     * 角色id，关联角色表
     */
    @TableField("role_id")
    private Long roleId;

    @TableField(value = "created_at")
    private LocalDateTime createdAt;

}
