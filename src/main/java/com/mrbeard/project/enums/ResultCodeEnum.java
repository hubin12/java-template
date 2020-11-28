package com.mrbeard.project.enums;

/**
 * 响应返回枚举类
 *
 * @author: hubin
 * @date: 2020/11/16 16:50
 */
public enum ResultCodeEnum {
    /**
     * 成功
     */
    SUCCESS(200,"成功!"),
    /**
     * 失败
     */
    FAIL(400,"失败!"),
    /**
     * 未认证（签名错误）
     */
    UNAUTHORIZED(401,"未鉴权!"),
    /**
     * 接口不存在
     */
    NOT_FOUND(404,"NotFind!"),
    /**
     * 参数异常
     */
    PARAM_ERROR(405,"参数异常!"),
    /**
     * 服务器内部错误
     */
    COMMON_SERVER_ERROR(500,"服务器异常!"),

    /**
     * 删除树木失败
     */
    DELETE_TREE_FAIL(501, "删除树木失败!"),

    /**
     * 资源未找到
     */
    RESOUCE_NOT_EXIST(502, "资源不存在!"),

    /**
     * 验证码不正确
     */
    LOGIN_CODE_ERROR(503, "验证码错误!"),

    /**
     * 用户名或密码不正确
     */
    USERNAME_OR_PASSWORD_ERROR(504, "用户名或密码不正确!"),

    /**
     * 树木已存在
     */
    RESOUCE_EXIST(505, "树木已存在!"),


    /**
     * 新增树木失败
     */
    ADD_TREE_FAIL(506, "新增树木失败!");

    /**
     * 响应code
     */
    public Integer code;
    /**
     * 响应codeName
     */
    public String message;

    ResultCodeEnum(Integer code,String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * code
     * @return
     */
    public static ResultCodeEnum getEnumByCode(Integer code) {
        ResultCodeEnum[] values = ResultCodeEnum.values();
        for (ResultCodeEnum resultCodeEnum : values){
            if(resultCodeEnum.code.equals(code)){
                return resultCodeEnum;
            }
        }
        return ResultCodeEnum.COMMON_SERVER_ERROR;
    }

    /**
     * code
     * @return
     */
    public Integer getCode() {
        return code;
    }

    /**
     * code
     * @return
     */
    public String getMessage() {
        return message;
    }
}
