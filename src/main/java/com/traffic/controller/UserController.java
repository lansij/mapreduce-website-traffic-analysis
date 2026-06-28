package com.traffic.controller;

import com.traffic.entity.User;
import com.traffic.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户接口
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Map<String, Object> register(@RequestParam String username,
                                        @RequestParam String password) {
        Map<String, Object> result = new HashMap<>();
        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            result.put("code", 400);
            result.put("msg", "用户名和密码不能为空");
            return result;
        }

        User user = userService.register(username.trim(), password);
        if (user != null) {
            result.put("code", 200);
            result.put("msg", "注册成功");
        } else {
            result.put("code", 400);
            result.put("msg", "用户名已存在");
        }
        return result;
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestParam String username,
                                     @RequestParam String password,
                                     HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User user = userService.login(username, password);
        if (user != null) {
            session.setAttribute("currentUser", user);
            result.put("code", 200);
            result.put("msg", "登录成功");
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("role", user.getRole());
            result.put("data", userInfo);
        } else {
            result.put("code", 400);
            result.put("msg", "用户名或密码错误");
        }
        return result;
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Map<String, Object> info(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User user = (User) session.getAttribute("currentUser");
        if (user != null) {
            result.put("code", 200);
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("role", user.getRole());
            result.put("data", userInfo);
        } else {
            result.put("code", 401);
            result.put("msg", "未登录");
        }
        return result;
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public Map<String, Object> logout(HttpSession session) {
        session.invalidate();
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "已退出登录");
        return result;
    }
}
