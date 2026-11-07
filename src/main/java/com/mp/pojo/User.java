package com.mp.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * @author 刘瑞麒
 * @date 2026/11/2 11:24
 *
 * 用户表
 */
@TableName("sys_user")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {

    /**
     * 主键id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名（不区分大小写）
     */
    private String username;

    /**
     * 密码哈希字符串（BCrypt）
     */
    @TableField(value = "password_hash")
    private String passwordHash;

    /**
     * 账号状态（0：禁用；1：启用，默认1）
     */
    private Integer status;

    @TableField(value = "created_at")
    private LocalDateTime createdAt;

    @TableField(value = "updated_at")
    private LocalDateTime updatedAt;

    //profile:read
    @TableField(exist = false)
    public Set<String> roles;

    @TableField(exist = false)
    public Set<String> permissions;
}
