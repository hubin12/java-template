package com.mrbeard.project.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * 获取表字段信息
 *
 * @author: hubin
 * @date: 2020/12/2 15:07
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DatabaseTableColumn {

    /**
     * 字段名
     */
    private String columnName;

    /**
     * 字段类型
     */
    private String dataType;

    /**
     * 字段备注
     */
    private String columnComment;

    /**
     * 主键标识
     */
    private String columnKey;


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DatabaseTableColumn column = (DatabaseTableColumn) o;
        return Objects.equals(dataType, column.dataType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataType);
    }
}
