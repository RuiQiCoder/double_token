package com.mp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mp.mapper.UserRoleMapper;
import com.mp.pojo.User_Role;
import com.mp.service.UserRoleService;
import org.springframework.stereotype.Service;

/**
 * @author 刘瑞麒
 * @date 2026/11/7 19:20
 */
@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, User_Role> implements UserRoleService {
}
