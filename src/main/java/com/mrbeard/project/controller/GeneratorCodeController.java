package com.mrbeard.project.controller;

import com.mrbeard.project.dto.request.GeneratorJavaCodeDTO;
import com.mrbeard.project.dto.request.GeneratorSqlCodeDTO;
import com.mrbeard.project.dto.request.ListTablesReqDTO;
import com.mrbeard.project.entity.common.Result;
import com.mrbeard.project.service.GeneratorCodeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * 代码生成控制器
 *
 * @author: hubin
 * @date: 2020/12/1 11:36
 */
@RestController
@RequestMapping("/api")
public class GeneratorCodeController {

    /**
     * 注入service
     */
    @Resource
    GeneratorCodeService generatorCodeService;


    /**
     * 生成SQL脚本
     *
     * @param reqDTO
     * @return
     */
    @PostMapping("/generatorSql")
    public Result generatorSql(@Valid @RequestBody GeneratorSqlCodeDTO reqDTO){
        return generatorCodeService.generatorSql(reqDTO);
    }


    /**
     * 生成Java代码
     *
     * @return
     */
    @PostMapping("/generatorJava")
    public Result generatorJava(@Valid @RequestBody GeneratorJavaCodeDTO reqDTO, HttpServletRequest request){
        return generatorCodeService.generatorJava(reqDTO, request);
    }


    /**
     * 获取数据库中的所有数据库名称
     *
     * @return
     */
    @PostMapping("/listDataBases")
    public Result listDataBases(){
        return generatorCodeService.listDataBases();
    }


    /**
     * 获取数据库中的所有表名称
     *
     * @return
     */
    @PostMapping("/listTables")
    public Result listTables(@Valid @RequestBody ListTablesReqDTO reqDTO){
        return generatorCodeService.listTables(reqDTO);
    }


    /**
     * 下载文件
     * @param path 路径
     * @param response 响应
     * @return
     */
    @GetMapping("/download")
    public void download(String path, HttpServletResponse response){
        generatorCodeService.download(path, response);
    }
}
