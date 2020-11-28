package com.mrbeard.project.mapper;

import com.mrbeard.project.entity.Permission;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author hubin
 */
@Mapper
public interface PermissionMapper {
    int deleteByPrimaryKey(Long id);

    int insert(Permission record);

    int insertSelective(Permission record);

    Permission selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Permission record);

    int updateByPrimaryKey(Permission record);
}