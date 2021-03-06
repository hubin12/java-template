package com.mrbeard.project.config;

import cn.hutool.core.util.StrUtil;
import com.mrbeard.project.enums.ResultCodeEnum;
import com.mrbeard.project.exception.CustomException;
import com.mrbeard.project.utils.JwtUtil;
import com.mrbeard.project.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 登录拦截器
 *
 * @author: hubin
 * @date: 2020/11/18 16:01
 */
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {

    /**
     * redis
     */
    @Resource
    RedisUtil redisUtil;


    /**
     * 请求进入到正式业务层之前进行拦截判断是登陆
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        //从request中获取到usertoken
        //String userToken = request.getHeader("token");
        ////先从redis中获取
        //boolean hasKey = redisUtil.hasKey("user:token:" + userToken);
        //if(!hasKey){
        //    //用户退出
        //    log.error("token is empty!");
        //    throw new CustomException(ResultCodeEnum.UNAUTHORIZED);
        //}
        //if ("/".equals(request.getRequestURI())) {
        //    try {
        //        response.sendRedirect(request.getContextPath() + "/index.html");
        //        return true;
        //    } catch (IOException e) {
        //        log.error("redirect to index fail!");
        //        throw new CustomException();
        //    }
        //}
        ////如果usertoken为空,返回错误。
        //if (StrUtil.isEmpty(userToken)) {
        //    //用户未登录
        //    log.error("token is empty!");
        //    throw new CustomException(ResultCodeEnum.UNAUTHORIZED);
        //}
        ////解析token
        //JwtUtil.validToken(userToken);
        return true;
    }


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
    }

}