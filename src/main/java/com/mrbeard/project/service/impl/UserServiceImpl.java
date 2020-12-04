package com.mrbeard.project.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mrbeard.project.dto.request.DeleteUserInfoReqDTO;
import com.mrbeard.project.dto.request.ListUserReqDTO;
import com.mrbeard.project.dto.request.SaveOrUpdateUserInfoReqDTO;
import com.mrbeard.project.dto.request.UpdatePasswordReqDTO;
import com.mrbeard.project.dto.request.UserLoginReqDTO;
import com.mrbeard.project.dto.response.ListUserRspDTO;
import com.mrbeard.project.dto.response.UserInfoGetRspDTO;
import com.mrbeard.project.dto.response.UserLoginRspDTO;
import com.mrbeard.project.entity.User;
import com.mrbeard.project.entity.common.Result;
import com.mrbeard.project.enums.ResultCodeEnum;
import com.mrbeard.project.mapper.UserMapper;
import com.mrbeard.project.mapper.UserRoleMapper;
import com.mrbeard.project.service.UserService;
import com.mrbeard.project.utils.JwtUtil;
import com.mrbeard.project.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sun.misc.BASE64Encoder;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户service实现类
 *
 * @author: hubin
 * @date: 2020/11/18 13:42
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    /**
     * 注入redis
     */
    @Resource
    RedisUtil redisUtil;

    /**
     * 注入userMapper
     */
    @Resource
    UserMapper userMapper;

    /**
     * 注入用户角色mapper
     */
    @Resource
    UserRoleMapper userRoleMapper;

    /**
     * 过期时间为一天
     * TODO 正式上线更换为15分钟
     */
    private static final long EXPIRE_TIME = 24 * 60 * 60 * 1000;

    /**
     * 获取验证码
     *
     * @return
     */
    @Override
    public Result getCode() {
        // 定义图形验证码的长和宽
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(400, 200);
        // 将验证码存入redis
        String code = lineCaptcha.getCode();
        log.info("code:" + code);
        // 设置过期时间5分钟
        redisUtil.set("user:code:" + code, code, 300);
        byte[] imageBytes = lineCaptcha.getImageBytes();
        BASE64Encoder encoder = new BASE64Encoder();
        String encode = encoder.encode(imageBytes);
        return Result.returnSuccessWithData(encode);
    }

    /**
     * 用户登录
     *
     * @param reqDTO
     * @return
     */
    @Override
    public Result login(UserLoginReqDTO reqDTO) {
        // 校验验证码
        Object code = redisUtil.get("user:code:" + reqDTO.getCode());
        if (ObjectUtil.isEmpty(code)) {
            log.error("Login code error!, code={}", reqDTO.getCode());
            return Result.returnWithCode(ResultCodeEnum.LOGIN_CODE_ERROR);
        }

        // 根据用户名和密码校验用户
        User user = userMapper.selectByUserNamePassword(reqDTO);
        if (ObjectUtil.isEmpty(user)) {
            log.error("username or password error!, userName = {}", reqDTO.getUserName());
            return Result.returnWithCode(ResultCodeEnum.USERNAME_OR_PASSWORD_ERROR);
        }

        // 更新用户登录时间
        userMapper.updateLoginTime(user);
        // 生成JWT
        String token = JwtUtil.createToken(user.getUserName(), user.getId(), EXPIRE_TIME);
        //生成redis
        redisUtil.set("user:token:"+token, token, EXPIRE_TIME);
        // 返回token
        UserLoginRspDTO rspDTO = new UserLoginRspDTO();
        BeanUtil.copyProperties(user, rspDTO);
        rspDTO.setToken(token);

        return Result.returnSuccessWithData(rspDTO);
    }

    /**
     * 获取用户信息
     *
     * @return
     */
    @Override
    public Result getUserInfo(HttpServletRequest request) {
        //获取用户id
        if (ObjectUtil.isEmpty(request.getHeader("token"))) {
            log.error("get User info params is error!");
            return Result.returnWithCode(ResultCodeEnum.PARAM_ERROR);
        }
        User userInfo = JwtUtil.validToken(request.getHeader("token"));

        User user = userMapper.selectByPrimaryKey(userInfo.getId());

        //获取用户角色信息
        List<String> roles = userRoleMapper.selectRolesByUserId(user.getId());
        UserInfoGetRspDTO rspDTO = new UserInfoGetRspDTO();
        BeanUtil.copyProperties(user, rspDTO);
        rspDTO.setRoles(roles);
        return Result.returnSuccessWithData(rspDTO);
    }

    /**
     * 新增、修改用户信息
     *
     * @param reqDTO
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result saveOrUpdateUserInfo(SaveOrUpdateUserInfoReqDTO reqDTO) {
        if (!ObjectUtil.isAllNotEmpty(reqDTO.getUserName(), reqDTO.getPhone(), reqDTO.getPassword())) {
            log.error("setUserInfo params is error! {}", reqDTO);
            return Result.returnWithCode(ResultCodeEnum.PARAM_ERROR);
        }

        User user = new User();
        BeanUtil.copyProperties(reqDTO, user);
        // 修改
        if (ObjectUtil.isNotEmpty(reqDTO.getId())) {
            user.setUpdateDate(new Date());
            user.setPassword(null);
            int i = userMapper.updateByPrimaryKeySelective(user);
            if (i < 0) {
                log.error("update userInfo fail!, data={}", reqDTO);
                return Result.returnWithCode(ResultCodeEnum.COMMON_SERVER_ERROR);
            }
            return Result.returnSuccess();
        }
        // 新增
        user.setId(IdUtil.getSnowflake(3, 5).nextId())
                .setCreateDate(new Date())
                .setUpdateDate(new Date());
        if ((ObjectUtil.isEmpty(reqDTO.getNickName()))) {
            user.setNickName(reqDTO.getUserName());
        }

        int m = userMapper.insert(user);
        if (m < 0) {
            log.error("insert userInfo fail!, data={}", reqDTO);
            return Result.returnWithCode(ResultCodeEnum.COMMON_SERVER_ERROR);
        }
        return Result.returnSuccess();
    }

    /**
     * 获取用户信息列表
     *
     * @param reqDTO
     * @return
     */
    @Override
    public Result listUser(ListUserReqDTO reqDTO) {
        // 设置默认page
        int pageNo = ObjectUtil.isEmpty(reqDTO.getPageNo()) ? 1 : reqDTO.getPageNo();
        int pageSize = ObjectUtil.isEmpty(reqDTO.getPageSize()) ? 1 : reqDTO.getPageSize();
        // 开启分页
        PageHelper.startPage(pageNo, pageSize);
        List<User> list = userMapper.selectBatchSelective(reqDTO);
        List<ListUserRspDTO> rspDTOList = new ArrayList<>();

        if (ObjectUtil.isNotEmpty(list)) {
            rspDTOList = list.stream().map(data -> {
                ListUserRspDTO userRspDTO = new ListUserRspDTO();
                BeanUtil.copyProperties(data, userRspDTO);
                userRspDTO.setId(data.getId().toString());
                return userRspDTO;
            }).collect(Collectors.toList());
        }

        // 返回信息
        PageInfo pageInfo = new PageInfo(rspDTOList);
        return Result.returnSuccessWithData(pageInfo);
    }

    /**
     * 删除用户信息
     *
     * @param reqDTO
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result deleteUserInfo(DeleteUserInfoReqDTO reqDTO) {
        if (ObjectUtil.isEmpty(reqDTO.getId())) {
            log.error("delete userInfo params error!, {}", reqDTO);
            return Result.returnWithCode(ResultCodeEnum.PARAM_ERROR);
        }
        //删除用户
        int i = userMapper.deleteByPrimaryKey(reqDTO.getId());
        if (i < 0) {
            log.error("delete userInfo fail!, id={}", reqDTO.getId());
            return Result.returnWithCode(ResultCodeEnum.COMMON_SERVER_ERROR);
        }
        return Result.returnSuccess();
    }

    /**
     * 修改用户密码
     *
     * @param reqDTO
     * @return
     */
    @Override
    public Result updatePassword(UpdatePasswordReqDTO reqDTO) {
        if (ObjectUtil.isEmpty(reqDTO.getId()) || ObjectUtil.isEmpty(reqDTO.getPassword())) {
            log.error("update Password param is error!");
            return Result.returnWithCode(ResultCodeEnum.PARAM_ERROR);
        }

        //更新密码
        User user = new User();
        BeanUtil.copyProperties(reqDTO, user);
        int i = userMapper.updateByPrimaryKeySelective(user);
        if (i < 0) {
            log.error("update password fail!, data={}", reqDTO);
            return Result.returnWithCode(ResultCodeEnum.COMMON_SERVER_ERROR);
        }
        return Result.returnSuccess();
    }

    /**
     * 退出登录
     *
     * @param request
     * @return
     */
    @Override
    public Result logout(HttpServletRequest request) {
        String token = request.getHeader("token");
        //获取用户信息
        if (ObjectUtil.isEmpty(token)) {
            log.error("get User info params is error!");
            return Result.returnWithCode(ResultCodeEnum.PARAM_ERROR);
        }

        //退出登录，将token删除
        User user = JwtUtil.validToken(token);
        redisUtil.del("user:token:"+token);

        return Result.returnSuccess();
    }

}
