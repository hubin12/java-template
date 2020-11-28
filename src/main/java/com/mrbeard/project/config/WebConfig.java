package com.mrbeard.project.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * web配置
 *
 * @author: hubin
 * @date: 2020/11/18 16:11
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 自定义登录拦截器
     *
     * @param registry 注册表
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/api/login", "/api/code", "/api/fileUpload", "/api/getImage",
                        "/index.html", "/static/**", "/favicon.ico");
    }


}
