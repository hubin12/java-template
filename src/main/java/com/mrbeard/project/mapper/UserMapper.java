package com.mrbeard.project.mapper;

import com.mrbeard.project.dto.request.ListUserReqDTO;
import com.mrbeard.project.dto.request.UserLoginReqDTO;
import com.mrbeard.project.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * UserMapper
 * @author hubin
 * @date 2020-12-5
 */
@Mapper
public interface UserMapper {

    /**
     *
     *
     * @param id
     * @return
     */
    int deleteByPrimaryKey(Long id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    User selectByUserNamePassword(UserLoginReqDTO reqDTO);

    int updateLoginTime(User user);

    List<User> selectBatchSelective(ListUserReqDTO reqDTO);
}