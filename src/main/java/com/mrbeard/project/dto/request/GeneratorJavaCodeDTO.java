package com.mrbeard.project.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 生成Java代码请求Dto
 *
 * @author: hubin
 * @date: 2020/12/2 14:47
 */
@Data
public class GeneratorJavaCodeDTO {

    /**
     * 数据库名
     */
    @NotBlank
    private String databaseName;

    /**
     * 表名
     */
    @NotBlank
    private String tableName;

    /**
     * 实体类名
     */
    @NotBlank
    private String entityName;

    /**
     * controller类名
     */
    private String controllerName;

    /**
     * 是否生成Controller
     */
    @NotNull
    private Integer isGeneratorController;

    /**
     * 是否生成service
     */
    @NotNull
    private Integer isGeneratorService;

    /**
     * service姓名
     */
    private String serviceName;

    /**
     * 生成的mapper方法
     */
    @NotNull
    private List<String> mapperNames;
}
