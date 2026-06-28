package com.traffic.controller;

import com.traffic.entity.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 页面路由
 */
@Controller
public class PageController {

    @GetMapping("/")
    public String index(HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return "forward:/login.html";
        }
        return "forward:/user/task.html";
    }

    @GetMapping("/admin/")
    public String admin() {
        return "forward:/admin/index.html";
    }
}
