package com.mrbeard.project.dto.request;

import lombok.Data;

/**
 * 修改密码请求实体
 *
 * @author: hubin
 * @date: 2020/11/26 10:14
 */
@Data
public class UpdatePasswordReqDTO {

    /**
     * id
     */
    private Long id;

    /**
     * 密码
     */
    private String password;
}
