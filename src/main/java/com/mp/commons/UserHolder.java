package com.mp.commons;

import com.mp.pojo.User;
import com.mp.pojo.UserInfo;

/**
 * @author 刘瑞麒
 * @date 2026/11/3 09:24
 */

/**
 * 实际项目应该新建UserInfo实体类，存放userId，头像链接，手机号，昵称信息
 * 但当前demo数据表没有这些字段，所以就将User作为ThreadLocal缓存内容
 */
public class UserHolder {
    private static ThreadLocal<UserInfo> threadLocal = new ThreadLocal<>();

    /**
     *在请求在filter的时候，调用setUser方法，将User对象缓存在ThreadLocal中
     */
    public static void setUser(UserInfo userInfo) {
        threadLocal.set(userInfo);
    }

    /**
     *请求到底Controller后，需要用到User的地方都可以调用getUser方法
     */
    public static UserInfo getUser() {
        return threadLocal.get();
    }

    /**
     * 请求结束时，一定要删除ThreadLocal中的数据（shiro项目一般在JwtFilter中）
     */
    public static void removeUser() {
        threadLocal.remove();
    }

}
