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

/**
 * @author 刘瑞麒
 * @date 2026/11/2 11:42
 *
 * 角色表
 */
@TableName("sys_role")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Role {

    /**
     * 主键id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 角色标识（字符串），例如 "admin"、"user"
     * role_code = admin
     */
    @TableField("role_code")
    private String roleCode;

    /**
     * 角色名称，例如管理员，用户
     * role_name = 管理员
     */
    @TableField("role_name")
    private String roleName;

    private String description;

    /**
     * 状态（0：禁用；1：启用，默认1）
     */
    private Integer status;

    @TableField(value = "created_at")
    private LocalDateTime createdAt;

    @TableField(value = "updated_at")
    private LocalDateTime updatedAt;
}
