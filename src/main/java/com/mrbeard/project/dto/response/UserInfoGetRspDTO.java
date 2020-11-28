package com.mrbeard.project.dto.response;

import lombok.Data;

import java.util.List;

/**
 * 获取用户信息响应实体
 *
 * @author: hubin
 * @date: 2020/11/21 9:57
 */
@Data
public class UserInfoGetRspDTO {


    /**
     * 用户Id
     */
    private Long id;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 角色列表
     */
    private List<String> roles;
}
