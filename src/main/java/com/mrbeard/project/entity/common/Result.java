package com.mrbeard.project.entity.common;

import com.mrbeard.project.enums.ResultCodeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 响应实体类
 *
 * @author: hubin
 * @date: 2020/11/16 16:52
 */
@Data
@Accessors(chain = true)
public class Result {

    private Integer code;
    private String message;
    private Object data;

    public Result() { }

    public static Result returnSuccessWithData(Object data) {
        Result result = new Result();
        result.code = ResultCodeEnum.SUCCESS.code;
        result.message = ResultCodeEnum.SUCCESS.message;
        result.setData(data);
        return result;
    }

    public static Result returnSuccess(){
        Result result = new Result();
        result.code = ResultCodeEnum.SUCCESS.code;
        result.message = ResultCodeEnum.SUCCESS.message;
        return result;
    }

    public static Result returnWithCode(ResultCodeEnum codeEnum){
        Result result = new Result();
        result.code = codeEnum.code;
        result.message = codeEnum.message;
        return result;
    }


}
