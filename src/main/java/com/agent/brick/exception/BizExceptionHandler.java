package com.agent.brick.exception;

import com.agent.brick.pojo.vo.JsonResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 全局异常捕获
 * @author cKnight
 * @since 2024/6/11
 */
@ControllerAdvice
@Slf4j
public class BizExceptionHandler {

    /**
     * 红色
     */
    private static final String ANSI_RED = "\u001B[31m%s\u001B[0m";
    private static final String BIZ_ERROR = String.format(ANSI_RED, "[biz_error]");
    private static final String SYS_ERROR = String.format(ANSI_RED, "[system_error]");


    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public JsonResult handlerException(Exception e){
        if (e instanceof BizException){
            //自定义业务异常
            BizException exception = (BizException) e;
            log.error("{}:", BIZ_ERROR, e);
            return JsonResult.buildCodeAndMsg(exception.getCode(), exception.getMsg());
        }else {
            //系统级异常
            log.error("{}:", SYS_ERROR, e);
            return JsonResult.buildCodeAndMsg(-1,"系统繁忙，请稍后重试");
        }
    }
}
