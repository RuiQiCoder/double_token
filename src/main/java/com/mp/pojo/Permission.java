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
 * @date 2026/11/2 11:51
 *
 * 权限表
 */
@TableName("sys_permission")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Permission {
    /**
     * 主键id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 权限标识，例如 user:read，对应 Shiro 权限字符串
     */
    @TableField("permission_code")
    private String permissionCode;

    /**
     * 权限名称
     */
    @TableField("permission_name")
    private String permissionName;

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
