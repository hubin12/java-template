package com.mrbeard.project.dto.response;

import lombok.Data;

import java.util.List;

/**
 * 用户登录响应DTO
 *
 * @author: hubin
 * @date: 2020/11/18 16:44
 */
@Data
public class UserLoginRspDTO {

    /**
     * 用户token
     */
    private String token;
}
