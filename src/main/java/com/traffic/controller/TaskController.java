package com.traffic.controller;

import com.traffic.entity.AnalysisResult;
import com.traffic.entity.AnalysisTask;
import com.traffic.entity.User;
import com.traffic.service.HadoopService;
import com.traffic.service.TaskService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分析任务接口
 */
@RestController
@RequestMapping("/api/task")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private HadoopService hadoopService;

    /**
     * 提交分析任务
     */
    @PostMapping("/submit")
    public Map<String, Object> submit(@RequestParam String taskName,
                                      @RequestParam String taskType,
                                      HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User user = (User) session.getAttribute("currentUser");

        if (taskName == null || taskName.trim().isEmpty()) {
            result.put("code", 400);
            result.put("msg", "任务名称不能为空");
            return result;
        }

        AnalysisTask task = taskService.createTask(user.getId(), taskName.trim(), taskType);
        result.put("code", 200);
        result.put("msg", "任务已提交");
        result.put("data", task);

        hadoopService.executeTask(task);

        return result;
    }

    /**
     * 获取当前用户的任务列表
     */
    @GetMapping("/list")
    public Map<String, Object> list(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User user = (User) session.getAttribute("currentUser");
        List<AnalysisTask> tasks = taskService.listByUserId(user.getId());
        result.put("code", 200);
        result.put("data", tasks);
        return result;
    }

    /**
     * 查询任务状态
     */
    @GetMapping("/{id}/status")
    public Map<String, Object> status(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        AnalysisTask task = taskService.getById(id);
        if (task != null) {
            result.put("code", 200);
            result.put("data", task);
        } else {
            result.put("code", 404);
            result.put("msg", "任务不存在");
        }
        return result;
    }

    /**
     * 获取任务结果
     */
    @GetMapping("/{id}/result")
    public Map<String, Object> result(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        AnalysisTask task = taskService.getById(id);
        if (task == null) {
            result.put("code", 404);
            result.put("msg", "任务不存在");
            return result;
        }
        if (!"FINISHED".equals(task.getStatus())) {
            result.put("code", 400);
            result.put("msg", "任务尚未完成");
            return result;
        }

        List<AnalysisResult> results = taskService.getResults(id);
        result.put("code", 200);
        result.put("data", results);
        result.put("taskType", task.getTaskType());
        return result;
    }
}
