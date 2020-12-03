package com.mrbeard.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
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
        //生成entity代码
        generatorEntityCode(reqDTO, codePath);
        //生成mapper层代码
        generatorMapperCode(reqDTO, codePath);
        //生成service层代码
        generatorServiceCode(reqDTO, codePath);
        //生成controller层代码
        generatorControllerCode(reqDTO, codePath);
        //设置下载路径
        return Result.returnSuccess();
    }

    /**
     * 生成Entity代码
     *
     * @param reqDTO
     * @param codePath
     */
    private void generatorEntityCode(GeneratorJavaCodeDTO reqDTO, String codePath) {
        //内容
        StringBuilder content = new StringBuilder();

        //生成文件夹
        String entityPath = "entity";
        String dir = getDir(codePath + File.separator + entityPath);

        //获取表字段信息
        List<DatabaseTableColumn> columns = dataBasesMapper.selectTableColumns(reqDTO);

        //包名
        content.append("package ").append("com.base.project.entity;\n\n");
        content.append("import lombok.AllArgsConstructor;\n").append("import lombok.Data;\n");
        content.append("import lombok.NoArgsConstructor;\n").append("import lombok.experimental.Accessors; \n\n");
        //导包
        for (DatabaseTableColumn column : columns) {
            if (StrUtil.containsAny(column.getDataType(), "decimal", "datetime", "date", "timestamp", "time")) {
                content.append("import ").append(getJavaPackageType(column.getDataType())).append(";\n\n");
            }
        }

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
            content.append("    /**\n     * ").append(column.getColumnComment()).append("\n     */\n");
            content.append("    private ").append(getJavaType(column.getDataType())).append(" ").append(columnName).append(";\n");
        }
        content.append("}");

        //写入文件
        String fileName = dir + File.separator + reqDTO.getEntityName() + ".java";
        File file = new File(fileName);
        byte[] bytes = content.toString().getBytes();
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
     * 生成Controller层代码
     *
     * @param reqDTO
     */
    private void generatorControllerCode(GeneratorJavaCodeDTO reqDTO, String codePath) {
    }


    /**
     * 生成Service层代码
     *
     * @param reqDTO
     */
    private void generatorServiceCode(GeneratorJavaCodeDTO reqDTO, String codePath) {
    }


    /**
     * 生成Mapper层代码
     *
     * @param reqDTO
     */
    private void generatorMapperCode(GeneratorJavaCodeDTO reqDTO, String codePath) {
        //生成文件夹
        String mapperPath = "mapper";
        String dir = getDir(codePath + File.separator + mapperPath);
        //生成XML
        String xmlContent = getXmlMapper(reqDTO);


    }

    /**
     * 生成xml字符串
     *
     * @param reqDTO
     * @return
     */
    private String getXmlMapper(GeneratorJavaCodeDTO reqDTO) {
        //xml所有数据
        StringBuilder xmlContent = new StringBuilder();

        //头部dtd
        String headContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n";
        xmlContent.append(headContent);
        //mapper baseMapper
        StringBuilder resultMapContent = new StringBuilder();

        //BaseCloumnList
        StringBuilder baseColumnContent = new StringBuilder();
        baseColumnContent.append("<sql id=\"Base_Column_List\">\n");

        //命名空间
        resultMapContent.append("<mapper namespace=\"com.base.project.mapper.").append(reqDTO.getEntityName()).append("Mapper\">\n");
        //BaseResultMap
        resultMapContent.append("<resultMap id=\"BaseResultMap\" type=\"com.mrbeard.project.entity.").append(reqDTO.getEntityName()).append("\">\n");
        resultMapContent.append("<constructor>\n");

        //获取表字段信息
        StringBuilder idContent = new StringBuilder();
        StringBuilder argContent = new StringBuilder();
        List<DatabaseTableColumn> columns = dataBasesMapper.selectTableColumns(reqDTO);
        StringBuilder cloumnList = new StringBuilder();
        for (DatabaseTableColumn column : columns) {
            if (ObjectUtil.isNotEmpty(column.getColumnKey())) {
                idContent.append("<idArg column=\"").append(column.getColumnName())
                        .append("\" javaType=\"").append(getJavaPackageType(column.getDataType()))
                        .append("\" jdbcType=\"").append(column.getDataType().toUpperCase()).append("\" />\n");
            } else {
                argContent.append("<arg column=\"").append(column.getColumnName())
                        .append("\" javaType=\"").append(getJavaPackageType(column.getDataType()))
                        .append("\" jdbcType=\"").append(column.getDataType().toUpperCase()).append("\" />\n");
            }
            cloumnList.append(column.getColumnName()).append(",");
        }
        baseColumnContent.append(cloumnList.toString(), 0, cloumnList.toString().length() - 1).append("\n").append("</sql>\n");
        resultMapContent.append(idContent).append(argContent);
        resultMapContent.append("</constructor>\n");
        xmlContent.append(resultMapContent).append(baseColumnContent);

        //根据返回的需要生成的mapper方法进行生成
        String sqlMethod = setSqlMethods(reqDTO);

        return null;
    }

    /**
     * 生成Mapper Method
     *
     * @param reqDTO
     * @return
     */
    private String setSqlMethods(GeneratorJavaCodeDTO reqDTO) {
        List<String> mapperNames = reqDTO.getMapperNames();
        //条件插入
        if (mapperNames.contains("insertSelective")) {
            String insertSelective = generatorInsertSelective(reqDTO);
        }
        if(mapperNames.contains("insertBatch")) {
            String insertBatch = generatorBatchInsert(reqDTO);
        }
        return null;
    }

    /**
     * 生成批量条件插入
     *
     * @param reqDTO
     * @return
     */
    private String generatorBatchInsert(GeneratorJavaCodeDTO reqDTO) {
        StringBuilder content = new StringBuilder();
        content.append("<insert id=\"insertBatch\" parameterType=\"java.util.List\"");
        content.append("insert into ").append(reqDTO.getTableName()).append("(\n");
        //获取表信息
        List<DatabaseTableColumn> columns = dataBasesMapper.selectTableColumns(reqDTO);
        //值信息
        StringBuilder valueContent = new StringBuilder();
        StringBuilder foreachContent = new StringBuilder();
        foreachContent.append("<foreach collection=\"list\" item=\"item\" open=\"(\" separator=\"),(\" close=\")\">\n");
        for (DatabaseTableColumn column : columns) {
            content.append(column.getDataType()).append(",");
            foreachContent.append("#{item.").append(removeSuffixAndToUp(column.getDataType())).append(",jdbcType=").append(column.getDataType().toUpperCase()).append("},");
        }
        content.append(valueContent.toString(), 0, valueContent.toString().length() -1).append(") values\n");
        content.append(valueContent).append("</trim>\n").append("</insert>\n");
        return content.toString();
    }

    /**
     * 生成条件插入Mapper
     *
     * @param reqDTO
     * @return
     */
    private String generatorInsertSelective(GeneratorJavaCodeDTO reqDTO) {
        StringBuilder content = new StringBuilder();
        content.append("<insert id=\"insertSelective\" parameterType=\"com.base.project.entity.").append(reqDTO.getEntityName()).append("\">\n");
        content.append("insert into ").append(reqDTO.getTableName()).append("\n");
        content.append("<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">\n");
        //获取表信息
        List<DatabaseTableColumn> columns = dataBasesMapper.selectTableColumns(reqDTO);
        //值信息
        StringBuilder valueContent = new StringBuilder();
        for (DatabaseTableColumn column : columns) {
            content.append(" <if test=\"").append(removeSuffixAndToUp(column.getColumnName()))
                    .append(" != null\">\n        ").append(column.getColumnName()).append(",\n      </if>\n");
            valueContent.append("<if test=\"").append(removeSuffixAndToUp(column.getColumnName()))
                    .append(" != null\">\n        #{").append(removeSuffixAndToUp(column.getColumnName()))
                    .append(",jdbcType=").append(column.getDataType().toUpperCase()).append("},\n      </if>\n");
        }
        content.append("</trim>\n").append("<trim prefix=\"values (\" suffix=\")\" suffixOverrides=\",\">\n");
        content.append(valueContent).append("</trim>\n").append("</insert>\n");
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
