package com.mrbeard.project.service;

import com.mrbeard.project.dto.request.DeleteUserInfoReqDTO;
import com.mrbeard.project.dto.request.ListUserReqDTO;
import com.mrbeard.project.dto.request.UpdatePasswordReqDTO;
import com.mrbeard.project.dto.request.UserInfoGetReqDTO;
import com.mrbeard.project.dto.request.SaveOrUpdateUserInfoReqDTO;
import com.mrbeard.project.dto.request.UserLoginReqDTO;
import com.mrbeard.project.entity.common.Result;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户service
 *
 * @author: hubin
 * @date: 2020/11/18 13:41
 */
public interface UserService {


    /**
     * 获取验证码
     * @return
     */
    Result getCode();

    /**
     * 用户登录
     *
     * @param reqDTO
     * @return
     */
    Result login(UserLoginReqDTO reqDTO);

    /**
     * 获取用户信息
     *
     * @param request
     * @return
     */
    Result getUserInfo(HttpServletRequest request);

    /**
     * 新增、修改用户信息
     *
     * @param reqDTO
     * @return
     */
    Result saveOrUpdateUserInfo(SaveOrUpdateUserInfoReqDTO reqDTO);

    /**
     * 获取用户列表
     *
     * @param reqDTO
     * @return
     */
    Result listUser(ListUserReqDTO reqDTO);

    /**
     * 删除用户信息
     *
     * @param reqDTO
     * @return
     */
    Result deleteUserInfo(DeleteUserInfoReqDTO reqDTO);

    /**
     * 修改用户密码
     *
     * @param reqDTO
     * @return
     */
    Result updatePassword(UpdatePasswordReqDTO reqDTO);

    /**
     * 退出登录
     *
     * @param request
     * @return
     */
    Result logout(HttpServletRequest request);

}
