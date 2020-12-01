package com.mrbeard.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import com.mrbeard.project.dto.request.GeneratorSqlCodeDTO;
import com.mrbeard.project.dto.request.GeneratorSqlCodeFieldInfo;
import com.mrbeard.project.entity.common.Result;
import com.mrbeard.project.enums.ResultCodeEnum;
import com.mrbeard.project.exception.CustomException;
import com.mrbeard.project.service.GeneratorCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
        String dirPath = getDir("download");
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
        String rootPath = getDir("download");
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
    private String getDir(String dirName) {
        // 在项目同一级目录存放文件
        String rootPath = File.separator + dirName;
        File file = new File(rootPath);
        if (!file.exists()) {
            FileUtil.mkdir(file);
        }
        return rootPath;
    }
}
