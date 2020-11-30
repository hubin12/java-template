package com.mrbeard.project.controller;

import com.mrbeard.project.dto.request.DeleteUserInfoReqDTO;
import com.mrbeard.project.dto.request.ListUserReqDTO;
import com.mrbeard.project.dto.request.UpdatePasswordReqDTO;
import com.mrbeard.project.dto.request.UserInfoGetReqDTO;
import com.mrbeard.project.dto.request.SaveOrUpdateUserInfoReqDTO;
import com.mrbeard.project.dto.request.UserLoginReqDTO;
import com.mrbeard.project.entity.common.Result;
import com.mrbeard.project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户相关controller
 *
 * @author: hubin
 * @date: 2020/11/18 8:43
 */
@RestController
@RequestMapping("/api")
public class UserController {

    /**
     * 注入UserService
     */
    @Autowired
    UserService userService;


    /**
     * 获取验证码
     */
    @PostMapping(value = "/code")
    public Result getCode() {
        return userService.getCode();
    }

    /**
     * 用户登录
     *
     * @param reqDTO
     * @return
     */
    @PostMapping("/login")
    public Result login(@RequestBody UserLoginReqDTO reqDTO){
        return userService.login(reqDTO);
    }

    /**
     * 退出登录
     *
     * @return
     */
    @PostMapping("/logout")
    public Result logout(HttpServletRequest request){
        return userService.logout(request);
    }


    /**
     * 获取用户信息
     *
     * @param request
     * @return
     */
    @PostMapping("/getUserInfo")
    public Result getUserInfo(HttpServletRequest request){
        return userService.getUserInfo(request);
    }

    /**
     * 新增、修改用户信息
     *
     * @param reqDTO
     * @return
     */
    @PostMapping("/saveOrUpdateUserInfo")
    public Result saveOrUpdateUserInfo(@RequestBody SaveOrUpdateUserInfoReqDTO reqDTO){
        return userService.saveOrUpdateUserInfo(reqDTO);
    }


    /**
     * 修改密码
     *
     * @param reqDTO
     * @return
     */
    @PostMapping("/updatePassword")
    public Result updatePassword(@RequestBody UpdatePasswordReqDTO reqDTO){
        return userService.updatePassword(reqDTO);
    }


    /**
     * 删除用户信息
     *
     * @param reqDTO
     * @return
     */
    @PostMapping("/deleteUserInfo")
    public Result deleteUserInfo(@RequestBody DeleteUserInfoReqDTO reqDTO){
        return userService.deleteUserInfo(reqDTO);
    }

    /**
     * 获取用户信息列表
     *
     * @return
     */
    @PostMapping("/listUser")
    public Result listUser(@RequestBody ListUserReqDTO reqDTO){
        return userService.listUser(reqDTO);
    }



}
