package com.mrbeard.project.dto.request;

import lombok.Data;

/**
 * 用户登录DTO
 *
 * @author: hubin
 * @date: 2020/11/18 15:53
 */
@Data
public class UserLoginReqDTO {

    /**
     * 用户名
     */
    private String userName;

    /**
     * 密码
     */
    private String password;

    /**
     * 登录验证码
     */
    private String code;

}
