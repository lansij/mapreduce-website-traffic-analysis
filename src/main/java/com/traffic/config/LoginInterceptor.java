package com.traffic.config;

import com.alibaba.fastjson2.JSON;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashMap;
import java.util.Map;

/**
 * 登录拦截器
 */
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("currentUser") != null) {
            return true;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        Map<String, Object> result = new HashMap<>();
        result.put("code", 401);
        result.put("msg", "未登录");
        response.getWriter().write(JSON.toJSONString(result));
        return false;
    }
}
