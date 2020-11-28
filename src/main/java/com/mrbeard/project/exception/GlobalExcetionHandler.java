package com.mrbeard.project.exception;

import com.mrbeard.project.entity.common.Result;
import com.mrbeard.project.enums.ResultCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 全局异常处理
 *
 * @author mrbeard
 * @date 2020/08/25
 */
@Slf4j
@ControllerAdvice
public class GlobalExcetionHandler {

    /**
     * 处理异常
     *
     * @param e 错误
     * @return {@link Result}
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result handleException(Exception e) {
        log.error("handleException:", e);
        if(e instanceof CustomException){
            Integer code = Integer.valueOf(e.getMessage());
            ResultCodeEnum resultCodeEnum  = ResultCodeEnum.getEnumByCode(code);
            return Result.returnWithCode(resultCodeEnum);
        }
        return Result.returnWithCode(ResultCodeEnum.COMMON_SERVER_ERROR);
    }

}