package com.traffic.controller;

import com.traffic.entity.User;
import com.traffic.entity.AnalysisTask;
import com.traffic.service.TaskService;
import com.traffic.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.*;

/**
 * 管理员接口
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private TaskService taskService;

    private boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        return user != null && "ADMIN".equals(user.getRole());
    }

    @GetMapping("/users")
    public Map<String, Object> users(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        if (!isAdmin(session)) { result.put("code", 403); result.put("msg", "权限不足"); return result; }
        List<User> users = userService.listAll();
        users.forEach(u -> u.setPassword("***"));
        result.put("code", 200);
        result.put("data", users);
        return result;
    }

    @DeleteMapping("/user/{id}")
    public Map<String, Object> deleteUser(@PathVariable Long id, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        if (!isAdmin(session)) { result.put("code", 403); result.put("msg", "权限不足"); return result; }
        User current = (User) session.getAttribute("currentUser");
        if (current.getId().equals(id)) { result.put("code", 400); result.put("msg", "不能删除自己"); return result; }
        userService.removeById(id);
        result.put("code", 200); result.put("msg", "删除成功");
        return result;
    }

    @GetMapping("/tasks")
    public Map<String, Object> tasks(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        if (!isAdmin(session)) { result.put("code", 403); result.put("msg", "权限不足"); return result; }
        result.put("code", 200); result.put("data", taskService.listAll());
        return result;
    }

    @DeleteMapping("/task/{id}")
    public Map<String, Object> deleteTask(@PathVariable Long id, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        if (!isAdmin(session)) { result.put("code", 403); result.put("msg", "权限不足"); return result; }
        taskService.deleteTask(id);
        result.put("code", 200); result.put("msg", "删除成功");
        return result;
    }

    @GetMapping("/data/info")
    public Map<String, Object> dataInfo(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        if (!isAdmin(session)) { result.put("code", 403); result.put("msg", "权限不足"); return result; }
        List<Map<String, Object>> files = new ArrayList<>();
        File folder = new File("data");
        if (folder.exists() && folder.isDirectory()) {
            for (File file : Objects.requireNonNull(folder.listFiles())) {
                Map<String, Object> info = new HashMap<>();
                info.put("name", file.getName());
                info.put("size", file.length());
                info.put("isDirectory", file.isDirectory());
                files.add(info);
            }
        }
        result.put("code", 200); result.put("data", files);
        return result;
    }

    @PostMapping("/cache/clear")
    public Map<String, Object> clearCache(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        if (!isAdmin(session)) { result.put("code", 403); result.put("msg", "权限不足"); return result; }
        result.put("code", 200); result.put("msg", "缓存已清除");
        return result;
    }
}
