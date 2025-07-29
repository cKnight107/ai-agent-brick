package com.agent.brick.interceptor;

import com.agent.brick.compant.AuthComponent;
import com.agent.brick.compant.RedisCacheComponent;
import com.agent.brick.constants.GlobalConstants;
import com.agent.brick.enums.BizCodeEnum;
import com.agent.brick.pojo.dto.InterceptorDto;
import com.agent.brick.pojo.vo.JsonResult;
import com.agent.brick.util.CommonUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Objects;

/**
 * 登录拦截器逻辑 此为具体逻辑 拦截可在具体微服务,自定义调用
 * @since 2024/6/11
 * @author cKnight
 */
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {

    public static ThreadLocal<InterceptorDto> loginThreadLocal = new ThreadLocal<>();

    @Resource
    private RedisCacheComponent redisCacheComponent;


    @Resource
    private AuthComponent authComponent;

    /**
     * 前置拦截器
     *
     * @param request  current HTTP request
     * @param response current HTTP response
     * @param handler  chosen handler to execute, for type and/or instance evaluation
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //略过测试请求
        if (HttpMethod.OPTIONS.toString().equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpStatus.NO_CONTENT.value());
            return true;
        }
        //判断 url 是否符合要求
        if (checkUrl(request.getRequestURI())) {
            //非法请求
            log.error("拦截器,拦截非法请求,url:{}", request.getRequestURI());
            CommonUtils.sendJsonMessage(response, JsonResult.buildResult(BizCodeEnum.ILLEGAL_REQUEST));
            return false;
        }
        return aiProcess(request, response);
    }

    private boolean aiProcess(HttpServletRequest request, HttpServletResponse response) {
        InterceptorDto interceptorDto = authComponent.checkToken(request);
        if (Objects.isNull(interceptorDto)) {
            return resultProcess(false, response, BizCodeEnum.SYS_USER_LOGIN_EXPIRE);
        }
        loginThreadLocal.set(interceptorDto);
        return true;
    }


    /**
     * 处理 result
     *
     * @param result
     * @param response
     * @param bizCodeEnum
     * @return
     */
    private boolean resultProcess(boolean result, HttpServletResponse response, BizCodeEnum bizCodeEnum) {
        if (result) {
            return result;
        }
        CommonUtils.sendJsonMessage(response, JsonResult.buildResult(bizCodeEnum));
        return false;
    }

    /**
     * 判断是否是非法url
     *
     * @param requestURI url
     * @return boolean
     */
    private boolean checkUrl(String requestURI) {
        return !requestURI.contains(GlobalConstants.URL_API)
                && !requestURI.contains(GlobalConstants.URL_SWAGGER)
                && !requestURI.contains("doc.html");
    }



    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        //后置操作
        loginThreadLocal.remove();
    }
}
