package com.mp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mp.commons.result.Result;
import com.mp.dto.LoginDTO;
import com.mp.pojo.User;
import com.mp.vo.LoginVO;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

/**
 * @author 刘瑞麒
 * @date 2026/11/2 14:12
 */
public interface UserService extends IService<User> {
    Result<LoginVO> login(@Valid LoginDTO loginDTO, HttpServletResponse response);

    Result<LoginVO> refresh(String refreshToken, HttpServletResponse response);
}
