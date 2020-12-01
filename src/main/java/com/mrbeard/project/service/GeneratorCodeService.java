package com.mrbeard.project.service;

import com.mrbeard.project.dto.request.GeneratorSqlCodeDTO;
import com.mrbeard.project.entity.common.Result;

import javax.servlet.http.HttpServletResponse;

/**
 * 代码生成service
 *
 * @author: hubin
 * @date: 2020/12/1 11:40
 */
public interface GeneratorCodeService {

    /**
     * 生成SQL脚本
     *
     * @param reqDTO
     * @return
     */
    Result generatorSql(GeneratorSqlCodeDTO reqDTO);


    /**
     * 下载文件
     * @param path 路径
     * @param response 响应
     * @return
     */
    void download(String path, HttpServletResponse response);

}
