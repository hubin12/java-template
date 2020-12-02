package com.mrbeard.project.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 获取数据库中的所有数据表
 *
 * @author: hubin
 * @date: 2020/12/2 14:12
 */
@Data
public class ListTablesReqDTO {

    /**
     * 数据库名
     */
    @NotBlank
    private String databaseName;
}
