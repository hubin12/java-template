package com.mrbeard.project.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 字段名信息
 *
 * @author: hubin
 * @date: 2020/12/1 12:59
 */
@Data
public class GeneratorSqlCodeFieldInfo {

    /**
     * 字段名
     */
    @NotBlank(message = "字段名称不能为空")
    private String fieldName;

    /**
     * 字段类型
     */
    @NotBlank
    private String fieldType;

    /**
     * 字段长度
     */
    @NotNull
    private Integer fieldLength;

    /**
     * 小数点长度
     */
    @NotNull
    private Integer fieldDecimalLength;

    /**
     * 字段是否可以为空 0-否 1-是
     */
    @NotNull
    private Integer fieldCanBeNull;

    /**
     * 字段是否为主键 0-否 1-是
     */
    @NotNull
    private Integer fieldIsKey;

    /**
     * 字段描述
     */
    private String fieldDescription;
}
