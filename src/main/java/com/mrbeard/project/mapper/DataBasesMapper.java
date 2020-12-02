package com.mrbeard.project.mapper;

import com.mrbeard.project.dto.request.GeneratorJavaCodeDTO;
import com.mrbeard.project.entity.DatabaseTableColumn;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 数据库操作相关
 *
 * @author: hubin
 * @date: 2020/12/2 14:06
 */
@Mapper
public interface DataBasesMapper {


    /**
     * 查询所有数据库
     * @return
     */
    List<String> selectDataBases();

    /**
     * 查询数据库中的所有表名
     * @param databaseName
     * @return
     */
    List<String> selectDataBaseTables(String databaseName);

    /**
     * 查询表字段信息
     * @param reqDTO
     * @return
     */
    List<DatabaseTableColumn> selectTableColumns(GeneratorJavaCodeDTO reqDTO);
}
