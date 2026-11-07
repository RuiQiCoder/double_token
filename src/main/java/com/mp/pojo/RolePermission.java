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
 * @date 2026/11/2 13:41
 */
@TableName("sys_role_permission")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RolePermission {
    /**
     * 角色id，关联角色表
     */
    @TableField("role_id")
    private Long roleId;

    /**
     * 权限id，关联权限表
     */
    @TableField("permission_id")
    private Long permissionId;

    @TableField(value = "created_at")
    private LocalDateTime createdAt;

}
