package com.mrbeard.project.dto.request;

import lombok.Data;

import java.util.List;

/**
 * 设置用户信息
 *
 * @author: hubin
 * @date: 2020/11/24 17:25
 */
@Data
public class SaveOrUpdateUserInfoReqDTO {

    /**
     * 用户Id
     */
    private Long id;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 密码
     */
    private String password;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 权限信息
     */
    private List<Long> permissions;
}
