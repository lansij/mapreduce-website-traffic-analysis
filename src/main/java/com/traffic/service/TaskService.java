package com.traffic.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.traffic.entity.AnalysisResult;
import com.traffic.entity.AnalysisTask;
import com.traffic.mapper.ResultMapper;
import com.traffic.mapper.TaskMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 分析任务业务逻辑
 */
@Service
public class TaskService extends ServiceImpl<TaskMapper, AnalysisTask> {

    @Autowired
    private ResultMapper resultMapper;

    /**
     * 创建分析任务
     */
    public AnalysisTask createTask(Long userId, String taskName, String taskType) {
        AnalysisTask task = new AnalysisTask();
        task.setUserId(userId);
        task.setTaskName(taskName);
        task.setTaskType(taskType);
        task.setStatus("PENDING");

        // 使用绝对路径，避免异步任务中工作目录不一致的问题
        String baseDir = new File("").getAbsolutePath();
        task.setInputPath(new File(baseDir, "data/access_log.csv").getAbsolutePath());
        task.setOutputPath(new File(baseDir, "data/output/" + taskType.toLowerCase() + "_" + System.currentTimeMillis()).getAbsolutePath());

        task.setCreateTime(LocalDateTime.now());
        save(task);
        return task;
    }

    /**
     * 获取用户的任务列表
     */
    public List<AnalysisTask> listByUserId(Long userId) {
        LambdaQueryWrapper<AnalysisTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AnalysisTask::getUserId, userId)
               .orderByDesc(AnalysisTask::getCreateTime);
        return list(wrapper);
    }

    /**
     * 更新任务状态
     */
    public void updateStatus(Long taskId, String status) {
        AnalysisTask task = getById(taskId);
        if (task != null) {
            task.setStatus(status);
            if ("FINISHED".equals(status) || "FAILED".equals(status)) {
                task.setFinishTime(LocalDateTime.now());
            }
            updateById(task);
        }
    }

    /**
     * 保存分析结果
     */
    public void saveResults(Long taskId, List<AnalysisResult> results) {
        for (AnalysisResult result : results) {
            result.setTaskId(taskId);
            resultMapper.insert(result);
        }
    }

    /**
     * 获取任务的结果列表
     */
    public List<AnalysisResult> getResults(Long taskId) {
        LambdaQueryWrapper<AnalysisResult> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AnalysisResult::getTaskId, taskId);
        return resultMapper.selectList(wrapper);
    }

    /**
     * 获取所有任务（管理员用）
     */
    public List<AnalysisTask> listAll() {
        return list(new LambdaQueryWrapper<AnalysisTask>()
                .orderByDesc(AnalysisTask::getCreateTime));
    }

    /**
     * 删除任务及其结果
     */
    public void deleteTask(Long taskId) {
        // 先删除结果
        LambdaQueryWrapper<AnalysisResult> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AnalysisResult::getTaskId, taskId);
        resultMapper.delete(wrapper);
        // 再删除任务
        removeById(taskId);
    }
}
