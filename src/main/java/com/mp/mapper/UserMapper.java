package com.mp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mp.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Set;

/**
 * @author 刘瑞麒
 * @date 2026/11/2 13:55
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    @Select("select distinct role_code from sys_role where id in (select distinct role_id from sys_user_role where user_id = #{userId});")
    Set<String> getRoles(Long userId);
    @Select("select distinct permission_code from sys_permission where id " +
            "in (select distinct permission_id from sys_role_permission where role_id " +
            "in (select distinct role_id from sys_user_role where user_id = #{userId}));")
    Set<String> getPermissions(Long userId);
}
