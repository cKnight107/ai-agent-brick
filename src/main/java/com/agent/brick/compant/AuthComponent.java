package com.agent.brick.compant;

import com.agent.brick.constants.GlobalConstants;
import com.agent.brick.enums.CacheKeyEnum;
import com.agent.brick.interceptor.LoginInterceptor;
import com.agent.brick.pojo.dto.InterceptorDto;
import com.agent.brick.pojo.dto.SysCacheUserDto;
import com.agent.brick.util.SecurityUtils;
import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 权限组件
 * @author cKnight
 * @since  2024/11
 */
@Component
@Slf4j
public class AuthComponent {

    @Resource
    private RedisCacheComponent redisCacheComponent;


    public SysCacheUserDto getAdminUserInfo(String token) {
        InterceptorDto interceptorDto = checkToken(token);
        if (Objects.isNull(interceptorDto)) {
            return null;
        }
        return getAdminUserInfo(interceptorDto);
    }

    public SysCacheUserDto getAdminUserInfo() {
        InterceptorDto interceptorDto = LoginInterceptor.loginThreadLocal.get();
        if (Objects.isNull(interceptorDto)){
            //不是后管用户
            return null;
        }
       return getAdminUserInfo(interceptorDto);
    }

    /**
     * 获取用户信息
     * @param interceptorDto 拦截信息
     * @return 用户信息
     */
    public SysCacheUserDto getAdminUserInfo(InterceptorDto  interceptorDto) {
        //从 threadLocal中获取token
        String token = interceptorDto.getToken();
        Long userId = interceptorDto.getUserId();
        String key = CacheKeyEnum.ADMIN_LOGIN_KEY.format(userId,token);
        String cache = redisCacheComponent.get(key);
        if (StringUtils.isBlank(cache)){
            return null;
        }
        return JSONObject.parseObject(cache,SysCacheUserDto.class);
    }




    public InterceptorDto checkToken(String token){
        //校验token
        JSONObject tokenData = SecurityUtils.AESDecryptJSON(token);
        if (Objects.isNull(tokenData)) {
            //解析失败
            log.info("登录拦截器，token解析失败:{}",token);
            return null;
        }
        Long id = tokenData.getLong("id");
        if (Objects.isNull(id)){
            log.info("登录拦截器，token解析成功但获取id失败:{}",tokenData);
            return null;
        }
        String key = CacheKeyEnum.ADMIN_LOGIN_KEY.format(id,token);
        //判断 token 是否过期
        String cache = redisCacheComponent.get(key);
        if (StringUtils.isBlank(cache)) {
            return null;
        }
        return InterceptorDto.builder().token(token).userId(id).build();
    }

    /**
     * 校验token
     * @param request req
     * @return InterceptorDto
     */
    public InterceptorDto checkToken(HttpServletRequest request){
        String token = request.getHeader(GlobalConstants.TOKEN);
        if (StringUtils.isBlank(token)) {
            token = request.getParameter(GlobalConstants.TOKEN);
            if (StringUtils.isBlank(token)) {
                return null;
            }
        }
        return checkToken(token);
    }
}
