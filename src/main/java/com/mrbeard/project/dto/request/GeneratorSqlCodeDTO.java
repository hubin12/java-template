package com.mrbeard.project.dto.request;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 生成SQL请求DTO
 *
 * @author: hubin
 * @date: 2020/12/1 11:41
 */
@Data
public class GeneratorSqlCodeDTO {

    /**
     * 表名
     */
    @NotBlank(message = "表名不能为空")
    private String tableName;

    /**
     * 数据库类型
     */
    private String databaseType;

    /**
     * 数据库名
     */
    private String databaseName;

    /**
     * 字段
     */
    @Valid
    @NotNull
    private List<GeneratorSqlCodeFieldInfo> fields;

}
