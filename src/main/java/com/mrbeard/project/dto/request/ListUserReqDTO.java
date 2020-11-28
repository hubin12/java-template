package com.mrbeard.project.dto.request;

import lombok.Data;

/**
 * 获取用户列表请求DTO
 *
 * @author: hubin
 * @date: 2020/11/25 11:25
 */
@Data
public class ListUserReqDTO {

    /**
     * 搜索字符串、可以是手机号、用户名或者邮箱
     */
    private String searchContent;

    /**
     * 页码
     */
    private Integer pageNo;

    /**
     * 每页大小
     */
    private Integer pageSize;

}
