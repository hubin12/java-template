package com.mrbeard.project.exception;

import com.mrbeard.project.enums.ResultCodeEnum;

/**
 * 自定义异常
 *
 * @author: hubin
 * @date: 2020/11/18 16:02
 */
public class CustomException extends RuntimeException{

    public CustomException(){
        super();
    }

    public CustomException(ResultCodeEnum code){
        super(code.getCode().toString());
    }
}
