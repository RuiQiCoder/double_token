package com.mp.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.util.Set;

/**
 * @author 刘瑞麒
 * @date 2026/11/3 14:38
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
public class UserInfo {
    private Long id;
    private String username;
    private Integer status;
    public Set<String> roles;
    public Set<String> permissions;
}
