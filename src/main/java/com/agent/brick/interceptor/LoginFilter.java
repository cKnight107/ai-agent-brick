package com.agent.brick.interceptor;

import com.agent.brick.compant.AuthComponent;
import com.agent.brick.enums.BizCodeEnum;
import com.agent.brick.exception.BizException;
import com.agent.brick.pojo.dto.InterceptorDto;
import com.agent.brick.pojo.vo.JsonResult;
import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.Resource;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author cKnight
 * @since 2025/11/14
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LoginFilter implements Filter {

    private static final ScopedValue<InterceptorDto> LOGIN_VALUE = ScopedValue.newInstance();

    @Resource
    private AuthComponent authComponent;

    // 使用 Spring 的 AntPathMatcher
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // 包含模式
    private final List<String> includePatterns = Arrays.asList(
            "/api/*/**"
    );

    // 排除模式
    private final List<String> excludePatterns = Arrays.asList(
            "/api/*/user/login",
            "/api/*/ai/completions"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestURI = httpRequest.getRequestURI();

        // 检查是否需要进行处理
        if (shouldProcess(requestURI)) {
            InterceptorDto interceptorDto = authComponent.checkToken(httpRequest);

            if (interceptorDto != null) {
                // 认证成功，绑定 ScopedValue 并继续
                ScopedValue.where(LOGIN_VALUE, interceptorDto).run(() -> {
                    try {
                        chain.doFilter(request, response);
                    } catch (Exception e) {
                        throw new RuntimeException("Filter processing error", e);
                    }
                });
                return;
            } else {
                // 认证失败
                handleUnauthorized((HttpServletResponse) response);
                return;
            }
        }
        // 不需要处理的请求直接放行
        chain.doFilter(request, response);
    }

    /**
     * 处理未授权请求
     */
    private void handleUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        // 构建错误响应
        String errorResponse = JSONObject.toJSONString(JsonResult.buildResult(BizCodeEnum.SYS_USER_LOGIN_EXPIRE));
        response.getWriter().write(errorResponse);
        response.getWriter().flush();
    }

    /**
     * 判断请求是否需要处理
     */
    private boolean shouldProcess(String requestURI) {
        // 检查是否匹配包含模式
        boolean shouldInclude = includePatterns.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, requestURI));

        if (!shouldInclude) {
            return false;
        }

        // 检查是否匹配排除模式
        boolean shouldExclude = excludePatterns.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, requestURI));

        return !shouldExclude;
    }

    public static InterceptorDto getInterceptorDto() {
        if (LOGIN_VALUE.isBound()) {
            return LOGIN_VALUE.get();
        }
        return null;
    }
}
