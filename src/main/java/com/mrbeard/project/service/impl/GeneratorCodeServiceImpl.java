package com.mrbeard.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.http.HttpUtil;
import com.mrbeard.project.dto.request.GeneratorJavaCodeDTO;
import com.mrbeard.project.dto.request.GeneratorSqlCodeDTO;
import com.mrbeard.project.dto.request.GeneratorSqlCodeFieldInfo;
import com.mrbeard.project.dto.request.ListTablesReqDTO;
import com.mrbeard.project.entity.DatabaseTableColumn;
import com.mrbeard.project.entity.User;
import com.mrbeard.project.entity.common.Result;
import com.mrbeard.project.enums.ResultCodeEnum;
import com.mrbeard.project.exception.CustomException;
import com.mrbeard.project.mapper.DataBasesMapper;
import com.mrbeard.project.mapper.UserMapper;
import com.mrbeard.project.service.GeneratorCodeService;
import com.mrbeard.project.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.descriptor.web.ContextService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 代码生成service实现类
 *
 * @author: hubin
 * @date: 2020/12/1 11:48
 */
@Slf4j
@Service
public class GeneratorCodeServiceImpl implements GeneratorCodeService {

    /**
     * userMapper
     */
    @Resource
    DataBasesMapper dataBasesMapper;

    /**
     * 生成SQL脚本
     *
     * @param reqDTO
     * @return
     */
    @Override
    public Result generatorSql(GeneratorSqlCodeDTO reqDTO) {
        //将请求转为map
        Map<String, Object> data = BeanUtil.beanToMap(reqDTO);
        //获取SQL字符串
        String content = setSqlStartContent(data) + setSqlFieldContent(data) + setSqlEndContent(data);
        byte[] bytes = content.getBytes();

        //保存文件到/download/sql
        String dirPath = getDir(File.separator + "download");
        String suffixPath = IdUtil.fastSimpleUUID().substring(0, 6) + "_" + reqDTO.getTableName() + ".sql";
        String fileName = dirPath + File.separator + suffixPath;
        File file = new File(fileName);
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            outputStream.write(bytes);
        } catch (Exception e) {
            log.error("write to file fail!");
            throw new CustomException(ResultCodeEnum.COMMON_SERVER_ERROR);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    log.error("write to file fail!");
                    throw new CustomException(ResultCodeEnum.COMMON_SERVER_ERROR);
                }
            }
        }
        return Result.returnSuccessWithData(suffixPath);
    }

    /**
     * 下载文件
     *
     * @param path     路径
     * @param response 响应
     * @return
     */
    @Override
    public void download(String path, HttpServletResponse response) {
        if (ObjectUtil.isEmpty(path)) {
            log.error("download path is error!");
            return;
        }
        String rootPath = getDir(File.separator + "download");
        File file = new File(rootPath + File.separator + path);
        if (!file.exists()) {
            log.error("file path is not exist!");
            return;
        }
        // 设置强制下载不打开
        response.setContentType("application/force-download");
        response.addHeader("Content-Disposition", "attachment;fileName=" + path);
        byte[] buffer = new byte[1024];
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        OutputStream outputStream = null;
        try {
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            outputStream = response.getOutputStream();
            int i = bis.read(buffer);
            while (i != -1) {
                outputStream.write(buffer, 0, i);
                i = bis.read(buffer);
            }
        } catch (Exception e) {
            log.error("file path is not exist!", e);
            return;
        } finally {
            try {
                outputStream.close();
                bis.close();
                fis.close();
            } catch (Exception e) {
                log.error("close stream fail!", e);
                return;
            }
        }

    }

    /**
     * 获取数据库中的所有表名称
     *
     * @param reqDTO
     * @return
     */
    @Override
    public Result listTables(ListTablesReqDTO reqDTO) {
        List<String> strings = dataBasesMapper.selectDataBaseTables(reqDTO.getDatabaseName());
        List<String> result = ObjectUtil.isEmpty(strings) ? new ArrayList<>() : strings;
        return Result.returnSuccessWithData(result);
    }

    /**
     * 获取数据库中的所有表名称
     *
     * @return
     */
    @Override
    public Result listDataBases() {
        List<String> strings = dataBasesMapper.selectDataBases();
        List<String> result = ObjectUtil.isEmpty(strings) ? new ArrayList<>() : strings;
        return Result.returnSuccessWithData(result);
    }

    /**
     * 生成Java代码
     *
     * @param reqDTO
     * @return
     */
    @Override
    public Result generatorJava(GeneratorJavaCodeDTO reqDTO, HttpServletRequest request) {
        //获取用户Id
        String token = request.getHeader("token");
        User user = JwtUtil.validToken(token);
        //包名路径
        String packagePath = "com" + File.separator + "base" + File.separator + "project";
        //创建代码基础路径
        String codePath = File.separator + "download" + File.separator + user.getId() + File.separator + packagePath;
        //获取字段信息
        List<DatabaseTableColumn> columns = dataBasesMapper.selectTableColumns(reqDTO);
        //生成entity代码
        generatorEntityCode(reqDTO, codePath, columns);
        //生成mapper层代码
        generatorMapperCode(reqDTO, codePath, columns);
        //生成service层代码
        if (reqDTO.getIsGeneratorService() == 1) {
            generatorServiceCode(reqDTO, codePath, columns);
        }
        //生成controller层代码
        if (reqDTO.getIsGeneratorController() == 1) {
            generatorControllerCode(reqDTO, codePath, columns);
        }
        //将代码打包
        String zipPath = user.getId() + ".zip";
        ZipUtil.zip(File.separator + "download" + File.separator + user.getId());
        //设置下载路径
        return Result.returnSuccessWithData(zipPath);
    }

    /**
     * 生成Entity代码
     *
     * @param reqDTO
     * @param codePath
     */
    private void generatorEntityCode(GeneratorJavaCodeDTO reqDTO, String codePath, List<DatabaseTableColumn> columns) {
        //内容
        StringBuilder content = new StringBuilder();

        //生成文件夹
        String entityPath = "entity";
        String dir = getDir(codePath + File.separator + entityPath);

        //包名
        content.append("package ").append("com.base.project.entity;\n\n");
        content.append("import lombok.AllArgsConstructor;\n").append("import lombok.Data;\n");
        content.append("import lombok.NoArgsConstructor;\n").append("import lombok.experimental.Accessors;\n\n");
        //导包
        String importString = new String();
        for (DatabaseTableColumn column : columns) {
            if (StrUtil.containsAny(column.getDataType(), "decimal", "datetime", "date", "timestamp", "time")) {
                if (importString != null && importString.contains("import " + getJavaPackageType(column.getDataType()) + ";\n\n")) {
                    continue;
                }
                importString = importString + "import " + getJavaPackageType(column.getDataType()) + ";\n\n";
            }
        }
        content.append(importString);
        //注释
        content.append("/**\n").append(" * ").append(reqDTO.getEntityName()).append("\n").append(" *\n");
        content.append(" * @author hubin\n").append(" * @date ");
        content.append(DateUtil.format(new Date(), "yyyy-MM-dd")).append("\n").append(" *\n").append(" */\n");

        //注解
        content.append("@Data\n").append("@Accessors(chain = true)\n");
        content.append("@AllArgsConstructor\n").append("@NoArgsConstructor\n");

        //主体
        content.append("public class ").append(reqDTO.getEntityName()).append(" {\n");
        for (DatabaseTableColumn column : columns) {
            String columnName = removeSuffixAndToUp(column.getColumnName());
            content.append("\t/**\n\t * ").append(column.getColumnComment()).append("\n\t */\n");
            content.append("\tprivate ").append(getJavaType(column.getDataType())).append(" ").append(columnName).append(";\n");
        }
        content.append("}");

        //写入文件
        String fileName = dir + File.separator + reqDTO.getEntityName() + ".java";
        writeFileToPath(reqDTO, content.toString(), fileName);
    }

    /**
     * 生成Controller层代码
     *
     * @param reqDTO
     */
    private void generatorControllerCode(GeneratorJavaCodeDTO reqDTO, String codePath, List<DatabaseTableColumn> columns) {
        //内容
        StringBuilder content = new StringBuilder();

        //判断DTO是否存在
        String dtoPath = "dto";
        File dtoFile = new File(codePath + File.separator + dtoPath + File.separator + reqDTO.getEntityName() + "DTO.java");
        if (!dtoFile.exists()) {
            //生成DTO
            generatorDtoCode(reqDTO, codePath, columns);
        }


        //判断Service是否存在
        String servicePath = "service";
        File serviceFile = new File(codePath + File.separator + servicePath + File.separator + reqDTO.getEntityName() + "Service.java");
        if (!serviceFile.exists()) {
            //生成service
            generatorServiceCode(reqDTO, codePath, columns);
        }

        content.append("package com.base.project.controller;\n\n");
        content.append("import com.base.project.entity.").append(reqDTO.getEntityName()).append(";\n");
        content.append("import com.base.project.dto.").append(reqDTO.getEntityName()).append("DTO;\n");
        content.append("import org.springframework.web.bind.annotation.RestController;\n");
        content.append("import org.springframework.web.bind.annotation.PostMapping;\n");
        content.append("import javax.annotation.Resource;\n");
        content.append("import java.util.List;\n\n");
        content.append("/**\n").append(" * ").append(ObjectUtil.isEmpty(reqDTO.getControllerName()) ? reqDTO.getEntityName() +"Controller" : reqDTO.getControllerName()).append("\n").append(" * @author hubin\n");
        content.append(" * @date ").append(DateUtil.format(new Date(), "yyyy-MM-dd")).append("\n").append(" */\n");
        content.append("@RestController\n");
        content.append("public class ").append(reqDTO.getEntityName()).append("Controller {\n");
        content.append("\t/**\n").append("\t * 注入").append(reqDTO.getEntityName()).append("Service\n").append("\t */\n").append("\t@Resource\n");
        content.append("\t").append(reqDTO.getEntityName()).append("Service ").append(firstToLowCase(reqDTO.getEntityName())).append("Service;\n\n");
        //条件插入
        if (reqDTO.getMapperNames().contains("insertSelective")) {
            content.append("\t/**\n").append("\t * 条件插入\n").append("\t *\n")
                    .append("\t * @param record 需要插入的数据\n").append("\t * @return 影响的条数\n").append("\t */\n")
                    .append("\t@PostMapping(value = \"/insertSelective\")\n");
            content.append("\tpublic int insertSelective(" + reqDTO.getEntityName() + "DTO record) {\n\n");
            content.append("\t\t// 调用Service\n");
            content.append("\t\treturn ").append(firstToLowCase(reqDTO.getEntityName())).append("Service.insertSelective(record);\n");
            content.append("\t}\n\n");
        }
        //批量插入
        if (reqDTO.getMapperNames().contains("insertBatch")) {
            content.append("\t/**\n").append("\t * 批量插入\n").append("\t *\n")
                    .append("\t * @param record 需要插入的数据\n").append("\t * @return 影响的条数\n").append("\t */\n")
                    .append("\t@PostMapping(value = \"/insertBatch\")\n");
            content.append("\tpublic int insertBatch(List<" + reqDTO.getEntityName() + "DTO> record) {\n\n");
            content.append("\t\t// 调用Service\n");
            content.append("\t\treturn ").append(firstToLowCase(reqDTO.getEntityName())).append("Service.insertBatch(record);\n");
            content.append("\t}\n\n");
        }
        //条件删除
        if (reqDTO.getMapperNames().contains("deleteSelective")) {
            content.append("\t/**\n").append("\t * 条件删除\n").append("\t *\n")
                    .append("\t * @param record 需要删除的数据\n").append("\t * @return 影响的条数\n").append("\t */\n")
                    .append("\t@PostMapping(value = \"/deleteSelective\")\n");
            content.append("\tpublic int deleteSelective(" + reqDTO.getEntityName() + "DTO record) {\n\n");
            content.append("\t\t// 调用Service \n");
            content.append("\t\treturn ").append(firstToLowCase(reqDTO.getEntityName())).append("Service.deleteSelective(record);\n");
            content.append("\t}\n\n");
        }
        //批量条件删除
        if (reqDTO.getMapperNames().contains("deleteBatchSelective")) {
            content.append("\t/**\n").append("\t * 批量条件删除\n").append("\t *\n")
                    .append("\t * @param record 需要删除的数据\n").append("\t * @return 影响的条数\n").append("\t */\n")
                    .append("\t@PostMapping(value = \"/deleteBatchSelective\")\n");
            content.append("\tpublic int deleteBatchSelective(List<" + reqDTO.getEntityName() + "DTO> record) {\n\n");
            content.append("\t\t// 调用Service\n");
            content.append("\t\treturn ").append(firstToLowCase(reqDTO.getEntityName())).append("Service.deleteBatchSelective(record);\n");
            content.append("\t}\n\n");
        }
        //条件更新
        if (reqDTO.getMapperNames().contains("updateSelective")) {
            content.append("\t/**\n").append("\t * 条件更新\n").append("\t *\n")
                    .append("\t * @param record 需要更新的数据\n").append("\t * @return 影响的条数\n").append("\t */\n")
                    .append("\t@PostMapping(value = \"/updateSelective\")\n");
            content.append("\tpublic int updateSelective(" + reqDTO.getEntityName() + "DTO record) {\n\n");
            content.append("\t\t// 调用Service\n");
            content.append("\t\treturn ").append(firstToLowCase(reqDTO.getEntityName())).append("Service.updateSelective(record);\n");
            content.append("\t}\n\n");
        }
        //批量条件更新
        if (reqDTO.getMapperNames().contains("updateBatchSelective")) {
            content.append("\t/**\n").append("\t * 批量条件更新\n").append("\t *\n")
                    .append("\t * @param record 需要更新的数据\n").append("\t * @return 影响的条数\n").append("\t */\n")
                    .append("\t@PostMapping(value = \"/updateBatchSelective\")\n");
            content.append("\tpublic int updateBatchSelective(List<" + reqDTO.getEntityName() + "DTO> record) {\n\n");
            content.append("\t\t// 调用Service\n");
            content.append("\t\treturn ").append(firstToLowCase(reqDTO.getEntityName())).append("Mapper.updateBatchSelective(record);\n");
            content.append("\t}\n\n");
        }
        //条件查询
        if (reqDTO.getMapperNames().contains("selectSelective")) {
            content.append("\t/**\n").append("\t * 条件查询\n").append("\t *\n")
                    .append("\t * @param record 需要查询数据\n").append("\t * @return 返回的数据\n").append("\t */\n")
                    .append("\t@PostMapping(value = \"/selectSelective\")\n");
            content.append("\tpublic List<" + reqDTO.getEntityName() + "> selectSelective(" + reqDTO.getEntityName() + "DTO record) {\n\n");
            content.append("\t\t// 调用Service\n");
            content.append("\t\treturn ").append(firstToLowCase(reqDTO.getEntityName())).append("Service.selectSelective(record);\n");
            content.append("\t}\n\n");
        }
        //批量条件查询
        if (reqDTO.getMapperNames().contains("selectBatchSelective")) {
            content.append("\t/**\n").append("\t * 批量条件查询\n").append("\t *\n")
                    .append("\t * @param record 需要更新的数据\n").append("\t * @return 影响的条数\n").append("\t */\n")
                    .append("\t@PostMapping(value = \"/selectBatchSelective\")\n");
            content.append("\tpublic List<" + reqDTO.getEntityName() + "> selectBatchSelective(List<" + reqDTO.getEntityName() + "DTO> record) {\n\n");
            content.append("\t\t// 调用Service\n");
            content.append("\t\treturn ").append(firstToLowCase(reqDTO.getEntityName())).append("Service.selectBatchSelective(record);\n");
            content.append("\t}\n\n");
        }
        content.append("}");

        //生成文件夹
        String controllerPath = "controller";
        String dir = getDir(codePath + File.separator + controllerPath);

        //写入文件
        String serviceFileName = dir + File.separator + (ObjectUtil.isEmpty(reqDTO.getControllerName()) ? reqDTO.getEntityName() + "Controller" : reqDTO.getControllerName()) + ".java";
        writeFileToPath(reqDTO, content.toString(), serviceFileName);
    }

    /**
     * 生成ServiceImpl
     *
     * @param reqDTO
     * @param codePath
     * @param columns
     */
    private void generatorServiceImplCode(GeneratorJavaCodeDTO reqDTO, String codePath, List<DatabaseTableColumn> columns) {
        StringBuilder content = new StringBuilder();
        content.append("package com.base.project.service.impl;\n\n");
        content.append("import com.base.project.entity.").append(reqDTO.getEntityName()).append(";\n");
        content.append("import com.base.project.dto.").append(reqDTO.getEntityName()).append("DTO;\n");
        content.append("import cn.hutool.core.bean.BeanUtil;\n");
        content.append("import java.util.List;\n\n");
        content.append("/**\n").append(" * ").append(ObjectUtil.isEmpty(reqDTO.getServiceName()) ? reqDTO.getEntityName() + "Service" : reqDTO.getServiceName()).append("Impl\n").append(" * @author hubin\n");
        content.append(" * @date ").append(DateUtil.format(new Date(), "yyyy-MM-dd")).append("\n").append(" */\n");
        content.append("@Service\n");
        content.append("public class ").append(reqDTO.getEntityName()).append("ServiceImpl implements ").append(reqDTO.getEntityName()).append("Service {\n");
        content.append("\t/**\n").append("\t * 注入").append(reqDTO.getEntityName()).append("Mapper\n").append("\t */\n").append("\t@Resource\n");
        content.append("\t").append(reqDTO.getEntityName()).append("Mapper ").append(firstToLowCase(reqDTO.getEntityName())).append("Mapper;\n\n");
        //条件插入
        if (reqDTO.getMapperNames().contains("insertSelective")) {
            content.append("\t/**\n").append("\t * 条件插入\n").append("\t *\n")
                    .append("\t * @param record 需要插入的数据\n").append("\t * @return 影响的条数\n").append("\t */\n").append("\t@Override\n");
            content.append("\tpublic int insertSelective(" + reqDTO.getEntityName() + "DTO record) {\n\n");
            content.append("\t\t// DTO转成实体类\n");
            content.append("\t\t").append(reqDTO.getEntityName()).append(" ").append(firstToLowCase(reqDTO.getEntityName())).append(" = new ").append(reqDTO.getEntityName()).append("();\n");
            content.append("\t\tBeanUtil.copyProperties(record, ").append(firstToLowCase(reqDTO.getEntityName())).append(");\n");
            content.append("\t\t// 调用Mapper\n");
            content.append("\t\treturn ").append(firstToLowCase(reqDTO.getEntityName())).append("Mapper.insertSelective(").append(firstToLowCase(reqDTO.getEntityName())).append(");\n");
            content.append("\t}\n\n");
        }
        //批量插入
        if (reqDTO.getMapperNames().contains("insertBatch")) {
            content.append("\t/**\n").append("\t * 批量插入\n").append("\t *\n")
                    .append("\t * @param record 需要插入的数据\n").append("\t * @return 影响的条数\n").append("\t */\n").append("\t@Override\n");
            content.append("\tpublic int insertBatch(List<" + reqDTO.getEntityName() + "DTO> record) {\n\n");
            content.append("\t\t// DTO转成实体类\n");
            content.append("\t\tList<").append(reqDTO.getEntityName()).append("> ").append(firstToLowCase(reqDTO.getEntityName())).append("List = new ArrayList<>();\n");
            content.append("\t\tBeanUtil.copyProperties(record, ").append(firstToLowCase(reqDTO.getEntityName())).append("List);\n");
            content.append("\t\t// 调用Mapper\n");
            content.append("\t\treturn ").append(firstToLowCase(reqDTO.getEntityName())).append("Mapper.insertBatch(").append(firstToLowCase(reqDTO.getEntityName())).append("List);\n");
            content.append("\t}\n\n");
        }
        //条件删除
        if (reqDTO.getMapperNames().contains("deleteSelective")) {
            content.append("\t/**\n").append("\t * 条件删除\n").append("\t *\n")
                    .append("\t * @param record 需要删除的数据\n").append("\t * @return 影响的条数\n").append("\t */\n").append("\t@Override\n");
            content.append("\tpublic int deleteSelective(" + reqDTO.getEntityName() + "DTO record) {\n\n");
            content.append("\t\t// DTO转成实体类\n");
            content.append("\t\t").append(reqDTO.getEntityName()).append(" ").append(firstToLowCase(reqDTO.getEntityName())).append(" = new ").append(reqDTO.getEntityName()).append("();\n");
            content.append("\t\tBeanUtil.copyProperties(record, ").append(firstToLowCase(reqDTO.getEntityName())).append(");\n");
            content.append("\t\t// 调用Mapper\n");
            content.append("\t\treturn ").append(firstToLowCase(reqDTO.getEntityName())).append("Mapper.deleteSelective(").append(firstToLowCase(reqDTO.getEntityName())).append(");\n");
            content.append("\t}\n\n");
        }
        //批量条件删除
        if (reqDTO.getMapperNames().contains("deleteBatchSelective")) {
            content.append("\t/**\n").append("\t * 批量条件删除\n").append("\t *\n")
                    .append("\t * @param record 需要删除的数据\n").append("\t * @return 影响的条数\n").append("\t */\n").append("\t@Override\n");
            content.append("\tpublic int deleteBatchSelective(List<" + reqDTO.getEntityName() + "DTO> record) {\n\n");
            content.append("\t\t// DTO转成实体类\n");
            content.append("\t\tList<").append(reqDTO.getEntityName()).append("> ").append(firstToLowCase(reqDTO.getEntityName())).append("List = new ArrayList<>();\n");
            content.append("\t\tBeanUtil.copyProperties(record, ").append(firstToLowCase(reqDTO.getEntityName())).append("List);\n");
            content.append("\t\t// 调用Mapper\n");
            content.append("\t\treturn ").append(firstToLowCase(reqDTO.getEntityName())).append("Mapper.deleteBatchSelective(").append(firstToLowCase(reqDTO.getEntityName())).append("List);\n");
            content.append("\t}\n\n");
        }
        //条件更新
        if (reqDTO.getMapperNames().contains("updateSelective")) {
            content.append("\t/**\n").append("\t * 条件更新\n").append("\t *\n")
                    .append("\t * @param record 需要更新的数据\n").append("\t * @return 影响的条数\n").append("\t */\n").append("\t@Override\n");
            content.append("\tpublic int updateSelective(" + reqDTO.getEntityName() + "DTO record) {\n\n");
            content.append("\t\t// DTO转成实体类\n");
            content.append("\t\t").append(reqDTO.getEntityName()).append(" ").append(firstToLowCase(reqDTO.getEntityName())).append(" = new ").append(reqDTO.getEntityName()).append("();\n");
            content.append("\t\tBeanUtil.copyProperties(record, ").append(firstToLowCase(reqDTO.getEntityName())).append(");\n");
            content.append("\t\t// 调用Mapper\n");
            content.append("\t\treturn ").append(firstToLowCase(reqDTO.getEntityName())).append("Mapper.updateSelective(").append(firstToLowCase(reqDTO.getEntityName())).append(");\n");
            content.append("\t}\n\n");
        }
        //批量条件更新
        if (reqDTO.getMapperNames().contains("updateBatchSelective")) {
            content.append("\t/**\n").append("\t * 批量条件删除\n").append("\t *\n")
                    .append("\t * @param record 需要更新的数据\n").append("\t * @return 影响的条数\n").append("\t */\n").append("\t@Override\n");
            content.append("\tpublic int updateBatchSelective(List<" + reqDTO.getEntityName() + "DTO> record) {\n\n");
            content.append("\t\t// DTO转成实体类\n");
            content.append("\t\tList<").append(reqDTO.getEntityName()).append("> ").append(firstToLowCase(reqDTO.getEntityName())).append("List = new ArrayList<>();\n");
            content.append("\t\tBeanUtil.copyProperties(record, ").append(firstToLowCase(reqDTO.getEntityName())).append("List);\n");
            content.append("\t\t// 调用Mapper\n");
            content.append("\t\treturn ").append(firstToLowCase(reqDTO.getEntityName())).append("Mapper.updateBatchSelective(").append(firstToLowCase(reqDTO.getEntityName())).append("List);\n");
            content.append("\t}\n\n");
        }
        //条件查询
        if (reqDTO.getMapperNames().contains("selectSelective")) {
            content.append("\t/**\n").append("\t * 条件查询\n").append("\t *\n")
                    .append("\t * @param record 需要查询数据\n").append("\t * @return 返回的数据\n").append("\t */\n").append("\t@Override\n");
            content.append("\tpublic List<" + reqDTO.getEntityName() + "> selectSelective(" + reqDTO.getEntityName() + "DTO record) {\n\n");
            content.append("\t\t// DTO转成实体类\n");
            content.append("\t\t").append(reqDTO.getEntityName()).append(" ").append(firstToLowCase(reqDTO.getEntityName())).append(" = new ").append(reqDTO.getEntityName()).append("();\n");
            content.append("\t\tBeanUtil.copyProperties(record, ").append(firstToLowCase(reqDTO.getEntityName())).append(");\n");
            content.append("\t\t// 调用Mapper\n");
            content.append("\t\treturn ").append(firstToLowCase(reqDTO.getEntityName())).append("Mapper.selectSelective(").append(firstToLowCase(reqDTO.getEntityName())).append(");\n");
            content.append("\t}\n\n");
        }
        //批量条件查询
        if (reqDTO.getMapperNames().contains("selectBatchSelective")) {
            content.append("\t/**\n").append("\t * 批量条件查询\n").append("\t *\n")
                    .append("\t * @param record 需要更新的数据\n").append("\t * @return 影响的条数\n").append("\t */\n").append("\t@Override\n");
            content.append("\tpublic List<" + reqDTO.getEntityName() + "> selectBatchSelective(List<" + reqDTO.getEntityName() + "DTO> record) {\n\n");
            content.append("\t\t// DTO转成实体类\n");
            content.append("\t\tList<").append(reqDTO.getEntityName()).append("> ").append(firstToLowCase(reqDTO.getEntityName())).append("List = new ArrayList<>();\n");
            content.append("\t\tBeanUtil.copyProperties(record, ").append(firstToLowCase(reqDTO.getEntityName())).append("List);\n");
            content.append("\t\t// 调用Mapper\n");
            content.append("\t\treturn ").append(firstToLowCase(reqDTO.getEntityName())).append("Mapper.selectBatchSelective(").append(firstToLowCase(reqDTO.getEntityName())).append("List);\n");
            content.append("\t}\n\n");
        }
        content.append("}");

        //生成文件夹
        String serviceImplPath = "service" + File.separator + "impl";
        String dir = getDir(codePath + File.separator + serviceImplPath);

        //写入文件
        String serviceFileName = dir + File.separator + (ObjectUtil.isEmpty(reqDTO.getServiceName()) ? reqDTO.getEntityName() + "Service" : reqDTO.getServiceName()) + "Impl.java";
        writeFileToPath(reqDTO, content.toString(), serviceFileName);
    }

    /**
     * 将字符串的第一个字母转成小学其余不变
     *
     * @param entityName
     * @return
     */
    private String firstToLowCase(String entityName) {
        String start = entityName.substring(0, 1).toLowerCase();
        String end = entityName.substring(1);
        return start + end;
    }

    /**
     * 生成DTO
     *
     * @param reqDTO
     * @param codePath
     * @param columns
     */
    private void generatorDtoCode(GeneratorJavaCodeDTO reqDTO, String codePath, List<DatabaseTableColumn> columns) {
        //内容
        StringBuilder content = new StringBuilder();

        //生成文件夹
        String entityPath = "dto";
        String dir = getDir(codePath + File.separator + entityPath);

        //包名
        content.append("package ").append("com.base.project.dto;\n\n");
        content.append("import lombok.AllArgsConstructor;\n").append("import lombok.Data;\n");
        content.append("import lombok.NoArgsConstructor;\n").append("import lombok.experimental.Accessors;\n\n");
        //导包
        String importString = new String();
        for (DatabaseTableColumn column : columns) {
            if (StrUtil.containsAny(column.getDataType(), "decimal", "datetime", "date", "timestamp", "time")) {
                if (importString != null && importString.contains("import " + getJavaPackageType(column.getDataType()) + ";\n\n")) {
                    continue;
                }
                importString = importString + "import " + getJavaPackageType(column.getDataType()) + ";\n\n";
            }
        }
        content.append(importString);

        //注释
        content.append("/**\n").append(" * ").append(reqDTO.getEntityName()).append("DTO\n").append(" *\n");
        content.append(" * @author hubin\n").append(" * @date ");
        content.append(DateUtil.format(new Date(), "yyyy-MM-dd")).append("\n").append(" *\n").append(" */\n");

        //注解
        content.append("@Data\n").append("@Accessors(chain = true)\n");
        content.append("@AllArgsConstructor\n").append("@NoArgsConstructor\n");

        //主体
        content.append("public class ").append(reqDTO.getEntityName()).append("DTO {\n");
        for (DatabaseTableColumn column : columns) {
            String columnName = removeSuffixAndToUp(column.getColumnName());
            content.append("\t/**\n\t * ").append(column.getColumnComment()).append("\n\t */\n");
            content.append("\tprivate ").append(getJavaType(column.getDataType())).append(" ").append(columnName).append(";\n");
        }
        content.append("}");

        //写入文件
        String fileName = dir + File.separator + reqDTO.getEntityName() + "DTO.java";
        writeFileToPath(reqDTO, content.toString(), fileName);
    }


    /**
     * 生成Service层代码
     *
     * @param reqDTO
     */
    private void generatorServiceCode(GeneratorJavaCodeDTO reqDTO, String codePath, List<DatabaseTableColumn> columns) {
        StringBuilder content = new StringBuilder();
        content.append("package com.base.project.service;\n\n");
        content.append("import com.base.project.entity.").append(reqDTO.getEntityName()).append(";\n");
        content.append("import com.base.project.dto.").append(reqDTO.getEntityName()).append("DTO;\n");
        content.append("import java.util.List;\n\n");
        content.append("/**\n").append(" * ").append(ObjectUtil.isEmpty(reqDTO.getServiceName()) ? reqDTO.getEntityName() + "Service" : reqDTO.getServiceName()).append("\n").append(" * @author hubin\n");
        content.append(" * @date ").append(DateUtil.format(new Date(), "yyyy-MM-dd")).append("\n").append(" */\n");
        content.append("public interface ").append(reqDTO.getEntityName()).append("Service {\n");
        //条件插入
        if (reqDTO.getMapperNames().contains("insertSelective")) {
            content.append("\t/**\n").append("\t * 条件插入\n").append("\t *\n")
                    .append("\t * @param record 需要插入的数据\n").append("\t * @return 影响的条数\n").append("\t */\n");
            content.append("\tint insertSelective(" + reqDTO.getEntityName() + "DTO record);\n\n");
        }
        //批量插入
        if (reqDTO.getMapperNames().contains("insertBatch")) {
            content.append("\t/**\n").append("\t * 批量插入\n").append("\t *\n")
                    .append("\t * @param record 需要插入的数据\n").append("\t * @return 影响的条数\n").append("\t */\n");
            content.append("\tint insertBatch(List<" + reqDTO.getEntityName() + "DTO> record);\n\n");
        }
        //条件删除
        if (reqDTO.getMapperNames().contains("deleteSelective")) {
            content.append("\t/**\n").append("\t * 条件删除\n").append("\t *\n")
                    .append("\t * @param record 需要删除的数据\n").append("\t * @return 影响的条数\n").append("\t */\n");
            content.append("\tint deleteSelective(" + reqDTO.getEntityName() + "DTO record);\n\n");
        }
        //批量条件删除
        if (reqDTO.getMapperNames().contains("deleteBatchSelective")) {
            content.append("\t/**\n").append("\t * 条件批量删除\n").append("\t *\n")
                    .append("\t * @param record 需要删除的数据\n").append("\t * @return 影响的条数\n").append("\t */\n");
            content.append("\tint deleteBatchSelective(List<" + reqDTO.getEntityName() + "DTO> record);\n\n");
        }
        //条件更新
        if (reqDTO.getMapperNames().contains("updateSelective")) {
            content.append("\t/**\n").append("\t * 条件更新\n").append("\t *\n")
                    .append("\t * @param record 需要更新的数据\n").append("\t * @return 影响的条数\n").append("\t */\n");
            content.append("\tint updateSelective(" + reqDTO.getEntityName() + "DTO record);\n\n");
        }
        //批量条件更新
        if (reqDTO.getMapperNames().contains("updateBatchSelective")) {
            content.append("\t/**\n").append("\t * 条件批量更新\n").append("\t *\n")
                    .append("\t * @param record 需要更新的数据\n").append("\t * @return 影响的条数\n").append("\t */\n");
            content.append("\tint updateBatchSelective(List<" + reqDTO.getEntityName() + "DTO> record);\n\n");
        }
        //条件查询
        if (reqDTO.getMapperNames().contains("selectSelective")) {
            content.append("\t/**\n").append("\t * 条件查询\n").append("\t *\n")
                    .append("\t * @param record 需要查询的数据\n").append("\t * @return 返回的数据\n").append("\t */\n");
            content.append("\tList<" + reqDTO.getEntityName() + "> selectSelective(" + reqDTO.getEntityName() + "DTO record);\n\n");
        }
        //批量条件查询
        if (reqDTO.getMapperNames().contains("selectBatchSelective")) {
            content.append("\t/**\n").append("\t * 条件批量查询\n").append("\t *\n")
                    .append("\t * @param record 需要查询的数据\n").append("\t * @return 返回的数据\n").append("\t */\n");
            content.append("\tList<" + reqDTO.getEntityName() + "> selectBatchSelective(List<" + reqDTO.getEntityName() + "DTO> record);\n\n");
        }
        content.append("}");

        //生成文件夹
        String mapperPath = "service";
        String dir = getDir(codePath + File.separator + mapperPath);

        //写入文件
        String serviceFileName = dir + File.separator + (ObjectUtil.isEmpty(reqDTO.getServiceName()) ? reqDTO.getEntityName() + "Service" : reqDTO.getServiceName()) + ".java";
        writeFileToPath(reqDTO, content.toString(), serviceFileName);

        //impl
        generatorServiceImplCode(reqDTO, codePath, columns);
    }


    /**
     * 生成Mapper层代码
     *
     * @param reqDTO
     */
    private void generatorMapperCode(GeneratorJavaCodeDTO reqDTO, String codePath, List<DatabaseTableColumn> columns) {
        //生成文件夹
        String mapperPath = "mapper";
        String dir = getDir(codePath + File.separator + mapperPath);
        //生成XML
        String xmlContent = getXmlMapper(reqDTO, columns);
        //生成MapperDao接口
        String daoContent = getDaoMapper(reqDTO, columns);

        //写入文件
        String xmlfileName = dir + File.separator + reqDTO.getEntityName() + "Mapper.xml";
        String daofileName = dir + File.separator + reqDTO.getEntityName() + "Mapper.java";
        writeFileToPath(reqDTO, xmlContent, xmlfileName);
        writeFileToPath(reqDTO, daoContent, daofileName);

    }

    /**
     * 生成dao接口
     *
     * @param reqDTO
     * @return
     */
    private String getDaoMapper(GeneratorJavaCodeDTO reqDTO, List<DatabaseTableColumn> columns) {
        StringBuilder content = new StringBuilder();
        content.append("package com.base.project.mapper;\n\n");
        content.append("import com.base.project.entity.").append(reqDTO.getEntityName()).append(";\n");
        content.append("import org.apache.ibatis.annotations.Mapper;\n\n");
        content.append("import java.util.List;\n\n");
        content.append("/**\n").append(" * ").append(reqDTO.getEntityName()).append("Mapper\n").append(" * @author hubin\n");
        content.append(" * @date ").append(DateUtil.format(new Date(), "yyyy-MM-dd")).append("\n").append(" */\n");
        content.append("@Mapper\n").append("public interface ").append(reqDTO.getEntityName()).append("Mapper {\n");
        //条件插入
        if (reqDTO.getMapperNames().contains("insertSelective")) {
            content.append("\t/**\n").append("\t * 条件插入\n").append("\t *\n")
                    .append("\t * @param record 需要插入的数据\n").append("\t * @return 影响的条数\n").append("\t */\n");
            content.append("\tint insertSelective(" + reqDTO.getEntityName() + " record);\n\n");
        }
        //批量插入
        if (reqDTO.getMapperNames().contains("insertBatch")) {
            content.append("\t/**\n").append("\t * 批量插入\n").append("\t *\n")
                    .append("\t * @param record 需要插入的数据\n").append("\t * @return 影响的条数\n").append("\t */\n");
            content.append("\tint insertBatch(List<" + reqDTO.getEntityName() + "> record);\n\n");
        }
        //条件删除
        if (reqDTO.getMapperNames().contains("deleteSelective")) {
            content.append("\t/**\n").append("\t * 条件删除\n").append("\t *\n")
                    .append("\t * @param record 需要删除的数据\n").append("\t * @return 影响的条数\n").append("\t */\n");
            content.append("\tint deleteSelective(" + reqDTO.getEntityName() + " record);\n\n");
        }
        //批量条件删除
        if (reqDTO.getMapperNames().contains("deleteBatchSelective")) {
            content.append("\t/**\n").append("\t * 条件批量删除\n").append("\t *\n")
                    .append("\t * @param record 需要删除的数据\n").append("\t * @return 影响的条数\n").append("\t */\n");
            content.append("\tint deleteBatchSelective(List<" + reqDTO.getEntityName() + "> record);\n\n");
        }
        //条件更新
        if (reqDTO.getMapperNames().contains("updateSelective")) {
            content.append("\t/**\n").append("\t * 条件更新\n").append("\t *\n")
                    .append("\t * @param record 需要更新的数据\n").append("\t * @return 影响的条数\n").append("\t */\n");
            content.append("\tint updateSelective(" + reqDTO.getEntityName() + " record);\n\n");
        }
        //批量条件更新
        if (reqDTO.getMapperNames().contains("updateBatchSelective")) {
            content.append("\t/**\n").append("\t * 条件批量更新\n").append("\t *\n")
                    .append("\t * @param record 需要更新的数据\n").append("\t * @return 影响的条数\n").append("\t */\n");
            content.append("\tint updateBatchSelective(List<" + reqDTO.getEntityName() + "> record);\n\n");
        }
        //条件查询
        if (reqDTO.getMapperNames().contains("selectSelective")) {
            content.append("\t/**\n").append("\t * 条件查询\n").append("\t *\n")
                    .append("\t * @param record 需要查询的数据\n").append("\t * @return 返回的数据\n").append("\t */\n");
            content.append("\tList<" + reqDTO.getEntityName() + "> selectSelective(" + reqDTO.getEntityName() + " record);\n\n");
        }
        //批量条件查询
        if (reqDTO.getMapperNames().contains("selectBatchSelective")) {
            content.append("\t/**\n").append("\t * 条件批量查询\n").append("\t *\n")
                    .append("\t * @param record 需要查询的数据\n").append("\t * @return 返回的数据\n").append("\t */\n");
            content.append("\tList<" + reqDTO.getEntityName() + "> selectBatchSelective(List<" + reqDTO.getEntityName() + "> record);\n\n");
        }
        content.append("}");
        return content.toString();
    }

    /**
     * 写入文件到磁盘
     *
     * @param reqDTO
     * @param xmlContent
     * @param fileName
     */
    private void writeFileToPath(GeneratorJavaCodeDTO reqDTO, String xmlContent, String fileName) {
        File file = new File(fileName);
        byte[] bytes = xmlContent.getBytes();
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            outputStream.write(bytes);
        } catch (Exception e) {
            log.error("write entity to dir fail!, {}", reqDTO.getEntityName(), e);
            throw new CustomException(ResultCodeEnum.FAILED_TO_WRITE_ENTITY);
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception e) {
                log.error("write entity to dir fail!, {}", reqDTO.getEntityName(), e);
                throw new CustomException(ResultCodeEnum.FAILED_TO_WRITE_ENTITY);
            }
        }
    }

    /**
     * 生成xml字符串
     *
     * @param reqDTO
     * @return
     */
    private String getXmlMapper(GeneratorJavaCodeDTO reqDTO, List<DatabaseTableColumn> columns) {
        //xml所有数据
        StringBuilder xmlContent = new StringBuilder();

        //头部dtd
        String headContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n";
        xmlContent.append(headContent);
        //mapper baseMapper
        StringBuilder resultMapContent = new StringBuilder();

        //BaseCloumnList
        StringBuilder baseColumnContent = new StringBuilder();
        baseColumnContent.append("\t<sql id=\"Base_Column_List\">\n");

        //命名空间
        resultMapContent.append("<mapper namespace=\"com.base.project.mapper.").append(reqDTO.getEntityName()).append("Mapper\">\n\n");
        //BaseResultMap
        resultMapContent.append("\t<resultMap id=\"BaseResultMap\" type=\"com.mrbeard.project.entity.").append(reqDTO.getEntityName()).append("\">\n");
        resultMapContent.append("\t\t<constructor>\n");

        //获取表字段信息
        StringBuilder idContent = new StringBuilder();
        StringBuilder argContent = new StringBuilder();
        StringBuilder cloumnList = new StringBuilder();
        for (DatabaseTableColumn column : columns) {
            if (ObjectUtil.isNotEmpty(column.getColumnKey())) {
                idContent.append("\t\t\t<idArg column=\"").append(column.getColumnName())
                        .append("\" javaType=\"").append(getJavaPackageType(column.getDataType()))
                        .append("\" jdbcType=\"").append(column.getDataType().toUpperCase()).append("\" />\n");
            } else {
                argContent.append("\t\t\t<arg column=\"").append(column.getColumnName())
                        .append("\" javaType=\"").append(getJavaPackageType(column.getDataType()))
                        .append("\" jdbcType=\"").append(column.getDataType().toUpperCase()).append("\" />\n");
            }
            cloumnList.append(column.getColumnName()).append(",");
        }
        baseColumnContent.append("\t\t").append(cloumnList.toString(), 0, cloumnList.toString().length() - 1).append("\n").append("\t</sql>\n");
        resultMapContent.append(idContent).append(argContent);
        resultMapContent.append("\t\t</constructor>\n").append("\t</resultMap>\n");
        xmlContent.append(resultMapContent).append(baseColumnContent);

        //根据返回的需要生成的mapper方法进行生成
        String sqlMethod = setSqlMethods(reqDTO, columns);
        xmlContent.append(sqlMethod).append("</mapper>\n");
        return xmlContent.toString();
    }

    /**
     * 生成Mapper Method
     *
     * @param reqDTO
     * @return
     */
    private String setSqlMethods(GeneratorJavaCodeDTO reqDTO, List<DatabaseTableColumn> columns) {
        List<String> mapperNames = reqDTO.getMapperNames();
        //条件插入
        String insertSelective = "";
        if (mapperNames.contains("insertSelective")) {
            insertSelective = generatorInsertSelective(reqDTO, columns);
        }
        //批量插入
        String insertBatch = "";
        if (mapperNames.contains("insertBatch")) {
            insertBatch = generatorBatchInsert(reqDTO, columns);
        }
        //条件删除
        String deleteSelective = "";
        if (mapperNames.contains("deleteSelective")) {
            deleteSelective = generatorDeleteSelective(reqDTO, columns);
        }
        //批量条件删除
        String deleteBatchSelective = "";
        if (mapperNames.contains("deleteBatchSelective")) {
            deleteBatchSelective = generatorDeleteBatchSelective(reqDTO, columns);
        }
        //条件更新
        String updateSelective = "";
        if (mapperNames.contains("updateSelective")) {
            updateSelective = generatorUpdateSelective(reqDTO, columns);
        }
        //批量条件更新
        String updateBatchSelective = "";
        if (mapperNames.contains("updateBatchSelective")) {
            updateBatchSelective = generatorUpdateBatchSelective(reqDTO, columns);
        }
        //条件查询
        String selectSelective = "";
        if (mapperNames.contains("selectSelective")) {
            selectSelective = generatorSelectSelective(reqDTO, columns);
        }
        //批量条件查询
        String selectBatchSelective = "";
        if (mapperNames.contains("selectBatchSelective")) {
            selectBatchSelective = generatorSelectBatchSelective(reqDTO, columns);
        }
        return insertSelective + insertBatch
                + deleteSelective + deleteBatchSelective
                + updateSelective + updateBatchSelective
                + selectSelective + selectBatchSelective;
    }

    /**
     * 生成批量条件查询SQL
     *
     * @param reqDTO
     * @return
     */
    private String generatorSelectBatchSelective(GeneratorJavaCodeDTO reqDTO, List<DatabaseTableColumn> columns) {
        StringBuilder content = new StringBuilder();
        content.append("\t<select id=\"selectBatchSelective\" resultMap=\"BaseResultMap\" parameterType=\"com.base.project.entity.").append(reqDTO.getEntityName()).append("\">\n");
        content.append("\t\tselect\n").append("\t\t<include refid=\"Base_Column_List\"/>\n");
        content.append("\t\tfrom ").append(reqDTO.getTableName()).append("\n").append("\t\twhere 1=0\n");
        content.append("\t\t<trim prefix=\"or\">\n").append("\t\t\t<foreach collection=\"list\" open=\"(\" separator=\")or(\" close=\")\" item=\"item\">\n");
        content.append("\t\t\t\t<trim prefixOverrides=\"and\">\n");
        for (DatabaseTableColumn column : columns) {
            content.append("\t\t\t\t\t<if test=\"item.").append(removeSuffixAndToUp(column.getColumnName()))
                    .append(" != null and item.").append(removeSuffixAndToUp(column.getColumnName()))
                    .append(" != ''\">\n\t\t\t\t\t\tand ").append(column.getColumnName()).append(" = #{item.")
                    .append(removeSuffixAndToUp(column.getColumnName())).append(",jdbcType=")
                    .append(column.getDataType().toUpperCase()).append("}\n\t\t\t\t\t</if>\n");
        }
        content.append("\t\t\t\t</trim>\n").append("\t\t\t</foreach>\n").append("\t\t</trim>\n").append("\t</select>\n\n");
        return content.toString();
    }

    /**
     * 生成条件查询SQL
     *
     * @param reqDTO
     * @return
     */
    private String generatorSelectSelective(GeneratorJavaCodeDTO reqDTO, List<DatabaseTableColumn> columns) {
        StringBuilder content = new StringBuilder();
        content.append("\t<select id=\"selectSelective\" resultMap=\"BaseResultMap\" parameterType=\"com.base.project.entity.").append(reqDTO.getEntityName()).append("\">\n");
        content.append("\t\tselect\n").append("\t\t<include refid=\"Base_Column_List\"/>\n");
        content.append("\t\tfrom ").append(reqDTO.getTableName()).append("\n").append("\t\t<where>\n");
        for (DatabaseTableColumn column : columns) {
            content.append("\t\t<if test=\"").append(removeSuffixAndToUp(column.getColumnName()))
                    .append(" != null and ").append(removeSuffixAndToUp(column.getColumnName()))
                    .append(" != ''\">\n\t\t\tand ").append(column.getColumnName()).append(" = #{")
                    .append(removeSuffixAndToUp(column.getColumnName())).append(",jdbcType=")
                    .append(column.getDataType().toUpperCase()).append("}\n\t\t</if>\n");
        }
        content.append("\t\t</where>\n").append("\t</select>\n\n");
        return content.toString();
    }

    /**
     * 生成批量条件更新SQL
     *
     * @param reqDTO
     * @return
     */
    private String generatorUpdateBatchSelective(GeneratorJavaCodeDTO reqDTO, List<DatabaseTableColumn> columns) {
        StringBuilder content = new StringBuilder();
        content.append("\t<update id=\"updateBatchSelective\" parameterType=\"java.util.List\">\n");
        content.append("\t\tupdate ").append(reqDTO.getTableName()).append("\n");
        content.append("\t\t\t<trim prefix=\"set\" suffixOverrides=\",\">\n");
        StringBuilder keyBuilder = new StringBuilder();
        for (DatabaseTableColumn column : columns) {
            if (ObjectUtil.isNotEmpty(column.getColumnKey())) {
                keyBuilder.append(column.getColumnName()).append("=#{i.").append(removeSuffixAndToUp(column.getColumnName())).append("} and ");
            }
            continue;
        }
        String keyString = keyBuilder.toString().trim().substring(0, keyBuilder.toString().trim().length() - 3);

        for (DatabaseTableColumn column : columns) {
            if (ObjectUtil.isEmpty(column.getColumnKey())) {
                content.append("\t\t\t\t<trim prefix=\"").append(column.getColumnName()).append(" = case\" suffix=\"end,\">\n")
                        .append("\t\t\t\t\t<foreach collection=\"list\" item=\"i\" index=\"index\">\n")
                        .append("\t\t\t\t\t\t<if test=\"i.").append(removeSuffixAndToUp(column.getColumnName())).append("!=null\">\n")
                        .append("\t\t\t\t\t\t\twhen ").append(keyString).append(" then #{i.")
                        .append(removeSuffixAndToUp(column.getColumnName())).append("}\n\t\t\t\t\t\t</if>\n")
                        .append("\t\t\t\t\t</foreach>\n").append("\t\t\t\t</trim>\n");
            }
            continue;
        }
        content.append("\t\t\t</trim>\n").append("\t\twhere\n").append("\t\t<foreach collection=\"list\" separator=\"or\" item=\"i\" index=\"index\">\n");
        content.append("\t\t\t").append(keyString).append("\n").append("\t\t</foreach>\n").append("\t</update>\n\n");
        return content.toString();
    }

    /**
     * 生成条件更新SQL
     *
     * @param reqDTO
     * @return
     */
    private String generatorUpdateSelective(GeneratorJavaCodeDTO reqDTO, List<DatabaseTableColumn> columns) {
        StringBuilder content = new StringBuilder();
        content.append("\t<update id=\"updateSelective\" parameterType=\"com.mrbeard.project.entity.").append(reqDTO.getEntityName()).append("\">\n");
        content.append("\t\tupdate ").append(reqDTO.getTableName()).append("\n");
        content.append("\t\t<set>\n");
        //保存主键信息
        List<DatabaseTableColumn> keys = new ArrayList<>();
        for (DatabaseTableColumn column : columns) {
            if (ObjectUtil.isEmpty(column.getColumnKey())) {
                content.append("\t\t\t<if test=\"").append(removeSuffixAndToUp(column.getColumnName())).append(" != null and ")
                        .append(removeSuffixAndToUp(column.getColumnName())).append(" != ''\">\n\t\t\t\t")
                        .append(column.getColumnName()).append(" = #{").append(removeSuffixAndToUp(column.getColumnName()))
                        .append(",jdbcType=").append(column.getDataType().toUpperCase()).append("},\n\t\t\t</if>\n");
            } else {
                keys.add(column);
            }
        }
        content.append("\t\t</set>\n\t\twhere ");
        StringBuilder keyString = new StringBuilder();
        for (DatabaseTableColumn key : keys) {
            keyString.append(key.getColumnName()).append(" = #{").append(removeSuffixAndToUp(key.getColumnName())).append(",jdbcType=").append(key.getDataType().toUpperCase()).append("} and ");
        }
        content.append(keyString.toString().trim(), 0, keyString.toString().trim().length() - 3).append("\n");
        content.append("\t</update>\n\n");
        return content.toString();
    }

    /**
     * 生成批量条件删除SQL
     *
     * @param reqDTO
     * @return
     */
    private String generatorDeleteBatchSelective(GeneratorJavaCodeDTO reqDTO, List<DatabaseTableColumn> columns) {
        StringBuilder content = new StringBuilder();
        content.append("\t<delete id=\"deleteBatchSelective\" parameterType=\"java.util.List\">\n");
        content.append("\t\tdelete from ").append(reqDTO.getTableName()).append("\n").append("\t\twhere 1=0\n");
        content.append("\t\t<trim prefix=\"or (\" suffix=\")\" prefixOverrides=\"and\">\n");
        content.append("\t\t\t<foreach collection=\"list\" item=\"item\" open=\"(\" close=\")\" separator=\")or(\">\n");
        content.append("\t\t\t\t<trim prefixOverrides=\"and\">\n");
        for (DatabaseTableColumn column : columns) {
            content.append("\t\t\t\t\t<if test=\"item.").append(removeSuffixAndToUp(column.getColumnName())).append(" != null and ")
                    .append(removeSuffixAndToUp(column.getColumnName()))
                    .append(" != ''\">\n\t\t\t\t\t\tand ").append(column.getColumnName()).append(" = #{item.")
                    .append(removeSuffixAndToUp(column.getColumnName())).append(",jdbcType=").append(column.getDataType().toUpperCase()).append("}\n\t\t\t\t\t</if>\n");
        }
        content.append("\t\t\t\t</trim>\n").append("\t\t\t</foreach>\n").append("\t\t</trim>\n").append("\t</delete>\n\n");
        return content.toString();
    }

    /**
     * 生成条件删除SQL
     *
     * @param reqDTO
     * @return
     */
    private String generatorDeleteSelective(GeneratorJavaCodeDTO reqDTO, List<DatabaseTableColumn> columns) {
        StringBuilder content = new StringBuilder();
        content.append("\t<delete id=\"deleteSelective\" parameterType=\"com.mrbeard.project.entity.").append(reqDTO.getEntityName()).append("\">\n");
        content.append("\t\tdelete from ").append(reqDTO.getTableName()).append("\n").append("\t\twhere 1=0\n");
        content.append("\t\t<trim prefix=\"or (\" suffix=\")\" prefixOverrides=\"and\">\n");
        for (DatabaseTableColumn column : columns) {
            content.append("\t\t\t<if test=\"").append(removeSuffixAndToUp(column.getColumnName())).append(" != null\">\n\t\t\t\tand ")
                    .append(column.getColumnName()).append(" = #{").append(removeSuffixAndToUp(column.getColumnName()))
                    .append(",jdbcType=").append(column.getDataType().toUpperCase()).append("}\n\t\t\t</if>\n");
        }
        content.append("\t\t</trim>\n\t</delete>\n\n");
        return content.toString();
    }

    /**
     * 生成批量条件插入SQL
     *
     * @param reqDTO
     * @return
     */
    private String generatorBatchInsert(GeneratorJavaCodeDTO reqDTO, List<DatabaseTableColumn> columns) {
        StringBuilder content = new StringBuilder();
        content.append("\t<insert id=\"insertBatch\" parameterType=\"java.util.List\">\n");
        content.append("\t\tinsert into ").append(reqDTO.getTableName()).append("(\n");
        //值信息
        StringBuilder valueContent = new StringBuilder();
        StringBuilder foreachContent = new StringBuilder();
        foreachContent.append("\t\t<foreach collection=\"list\" item=\"item\" open=\"(\" separator=\"),(\" close=\")\">\n");
        foreachContent.append("\t\t\t\t<trim suffixOverrides=\",\">\n");
        for (DatabaseTableColumn column : columns) {
            valueContent.append(column.getColumnName()).append(",");
            foreachContent.append("\t\t\t\t#{item.").append(removeSuffixAndToUp(column.getColumnName())).append(",jdbcType=").append(column.getDataType().toUpperCase()).append("},\n");
        }
        content.append("\t\t\t").append(valueContent.toString(), 0, valueContent.toString().length() - 1).append(") values\n");
        content.append(foreachContent);
        content.append("\t\t\t\t</trim>\n").append("\t\t</foreach>\n").append("\t</insert>\n\n");
        return content.toString();
    }

    /**
     * 生成条件插入SQL
     *
     * @param reqDTO
     * @return
     */
    private String generatorInsertSelective(GeneratorJavaCodeDTO reqDTO, List<DatabaseTableColumn> columns) {
        StringBuilder content = new StringBuilder();
        content.append("\t<insert id=\"insertSelective\" parameterType=\"com.base.project.entity.").append(reqDTO.getEntityName()).append("\">\n");
        content.append("\t\tinsert into ").append(reqDTO.getTableName()).append("\n");
        content.append("\t\t<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">\n");
        //值信息
        StringBuilder valueContent = new StringBuilder();
        for (DatabaseTableColumn column : columns) {
            content.append("\t\t\t<if test=\"").append(removeSuffixAndToUp(column.getColumnName()))
                    .append(" != null and ").append(removeSuffixAndToUp(column.getColumnName()))
                    .append(" != ''").append("\">\n\t\t\t\t").append(column.getColumnName()).append(",\n\t\t\t</if>\n");
            valueContent.append("\t\t\t<if test=\"").append(removeSuffixAndToUp(column.getColumnName()))
                    .append(" != null and ").append(removeSuffixAndToUp(column.getColumnName()))
                    .append(" != ''").append("\">\n\t\t\t\t#{").append(removeSuffixAndToUp(column.getColumnName()))
                    .append(",jdbcType=").append(column.getDataType().toUpperCase()).append("},\n\t\t\t</if>\n");
        }
        content.append("\t\t</trim>\n").append("\t\t<trim prefix=\"values (\" suffix=\")\" suffixOverrides=\",\">\n");
        content.append(valueContent).append("\t\t</trim>\n").append("\t</insert>\n\n");
        return content.toString();
    }

    /**
     * 将数据库类型转换为Java包类型
     *
     * @param dataType
     * @return
     */
    private String getJavaPackageType(String dataType) {
        switch (dataType.toLowerCase()) {
            case "varchar":
            case "char":
            case "text":
                return "java.lang.String";
            case "decimal":
                return "java.math.BigDecimal";
            case "datetime":
            case "date":
                return "java.util.Date";
            case "timestamp":
                return "java.sql.Timestamp";
            case "time":
                return "java.sql.Time";
            case "double":
                return "java.lang.Double";
            case "float":
                return "java.lang.Float";
            case "bigint":
                return "java.lang.Long";
            case "int":
            case "tinyint":
                return "java.lang.Integer";
            default:
                return "java.lang.*";
        }
    }

    /**
     * 将数据库类型转换为Java类型
     *
     * @param dataType
     * @return
     */
    private String getJavaType(String dataType) {
        switch (dataType.toLowerCase()) {
            case "varchar":
            case "char":
            case "text":
                return "String";
            case "timestamp":
                return "Timestamp";
            case "time":
                return "Time";
            case "datetime":
            case "date":
                return "Date";
            case "decimal":
                return "BigDecimal";
            case "double":
                return "Double";
            case "float":
                return "Float";
            case "bigint":
                return "Long";
            case "int":
            case "tinyint":
                return "Integer";
            default:
                return "String";
        }
    }


    /**
     * 生成开始内容字符串
     *
     * @return
     */
    private String setSqlStartContent(Map<String, Object> data) {
        StringBuilder builder = new StringBuilder();
        builder.append("-- ----------------------------\n");
        builder.append("-- The SQL is generator by Mr.beard\n");
        builder.append("-- creat date: {create_date}\n");
        builder.append("-- welcome to star: https://github.com/hubin12/java-template\n");
        builder.append("-- ----------------------------\n\n\n");
        builder.append("USE `{databaseName}`;\n\n\n");
        builder.append("SET NAMES utf8mb4;").append("\n");
        builder.append("SET FOREIGN_KEY_CHECKS = 0;\n\n");
        builder.append("-- ----------------------------\n");
        builder.append("-- Table structure for {tableName}\n");
        builder.append("-- ----------------------------\n");
        builder.append("DROP TABLE IF EXISTS `{tableName}`;\n");
        builder.append("CREATE TABLE `{tableName}`  (\n");
        String startContent = builder.toString();
        startContent = startContent.replaceAll("\\{create_date\\}", DateUtil.format(new Date(), "yyyy-MM-dd"));
        startContent = startContent.replaceAll("\\{tableName\\}", data.get("tableName").toString());
        startContent = startContent.replaceAll("\\{databaseName\\}", data.get("databaseName").toString());
        return startContent;
    }

    /**
     * 设置中间字段部分
     *
     * @param data
     * @return
     */
    private String setSqlFieldContent(Map<String, Object> data) {
        List<GeneratorSqlCodeFieldInfo> fields = (List<GeneratorSqlCodeFieldInfo>) data.get("fields");
        String fieldContent = new String();
        String keyContent = new String();

        //针对每个字段进行设置
        boolean hasKeyFlag = false;
        for (GeneratorSqlCodeFieldInfo field : fields) {
            StringBuilder builder = new StringBuilder();
            builder.append("`{fieldName}` ").append("{fieldType} ").append("{fieldCanBeNull} ");
            builder.append("COMMENT '{fieldDescription}',");
            String tempfieldContent = builder.toString();

            tempfieldContent = tempfieldContent.replaceAll("\\{fieldName\\}", field.getFieldName());
            String nullDefault = field.getFieldCanBeNull() == 1 ? "NULL DEFAULT NULL" : "NOT NULL";
            tempfieldContent = tempfieldContent.replaceAll("\\{fieldCanBeNull\\}", nullDefault);
            tempfieldContent = tempfieldContent.replaceAll("\\{fieldDescription\\}", field.getFieldDescription());
            switch (field.getFieldType()) {
                case "BIGINT":
                case "INT":
                case "TINYINT":
                    tempfieldContent = tempfieldContent.replaceAll("\\{fieldType\\}", field.getFieldType() + "(" + field.getFieldLength() + ")");
                    break;
                case "FLOAT":
                case "DECIMAL":
                case "DOUBLE":
                    tempfieldContent = tempfieldContent.replaceAll("\\{fieldType\\}", field.getFieldType() + "(" + field.getFieldLength() + "," + field.getFieldDecimalLength() + ")");
                    break;
                case "TIMESTAMP":
                case "DATETIME":
                case "TIME":
                    tempfieldContent = tempfieldContent.replaceAll("\\{fieldType\\}", field.getFieldType() + "(0)");
                    break;
                case "DATE":
                    tempfieldContent = tempfieldContent.replaceAll("\\{fieldType\\}", field.getFieldType());
                    break;
                case "CHAR":
                case "VARCHAR":
                    tempfieldContent = tempfieldContent.replaceAll("\\{fieldType\\}", field.getFieldType() + "(" + field.getFieldLength() + ") CHARACTER SET utf8 COLLATE utf8_general_ci");
                    break;
                case "TEXT":
                    tempfieldContent = tempfieldContent.replaceAll("\\{fieldType\\}", field.getFieldType() + "CHARACTER SET utf8 COLLATE utf8_general_ci");
                    break;
            }
            if (field.getFieldIsKey() == 1) {
                keyContent = keyContent + "`" + field.getFieldName() + "`,";
                hasKeyFlag = true;
            }
            fieldContent = fieldContent + tempfieldContent + "\n";
        }

        //判断是否有主键
        if (!hasKeyFlag) {
            log.error("don't has primary key!");
            throw new CustomException(ResultCodeEnum.LOST_PRIMARY_KEY);
        }
        keyContent = "PRIMARY KEY (" + keyContent.substring(0, keyContent.length() - 1) + ") USING BTREE\n";
        fieldContent = fieldContent + keyContent + ") ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;\n\n";
        return fieldContent;
    }


    /**
     * 生成结束内容字符串
     *
     * @return
     */
    private String setSqlEndContent(Map<String, Object> data) {
        StringBuilder builder = new StringBuilder();
        builder.append("-- ----------------------------\n");
        builder.append("-- Records of {tableName}\n");
        builder.append("-- ----------------------------\n\n");
        builder.append("SET FOREIGN_KEY_CHECKS = 1;").append("\n");
        String endContent = builder.toString();
        endContent = endContent.replaceAll("\\{tableName\\}", data.get("tableName").toString());
        return endContent;
    }


    /**
     * 根据文件夹名称生成路径
     *
     * @return
     * @throws FileNotFoundException
     */
    private static String getDir(String dirName) {
        // 在项目同一级目录存放文件
        FileUtil.mkdir(dirName);
        return dirName;
    }


    /**
     * 去除字符串中的下划线并将下划线后的字母变成大写
     *
     * @param columnName
     * @return
     */
    private String removeSuffixAndToUp(String columnName) {
        String[] strings = StrUtil.split(columnName, "_");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < strings.length; i++) {
            if (i == 0) {
                builder.append(strings[i]);
                continue;
            }
            String startStr = strings[i].substring(0, 1).toUpperCase();
            String endString = strings[i].substring(1).toLowerCase();
            builder.append(startStr + endString);
        }
        return builder.toString();
    }
}
