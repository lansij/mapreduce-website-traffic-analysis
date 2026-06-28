package com.traffic.service;

import com.traffic.entity.AnalysisResult;
import com.traffic.entity.AnalysisTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Hadoop MapReduce 任务调度服务
 * 每次提交任务同时执行三种分析：排行、时间峰值、IP分布
 */
@Service
public class HadoopService {

    @Autowired
    private TaskService taskService;

    /**
     * 异步执行分析任务（同时运行三种分析）
     */
    @Async
    public void executeTask(AnalysisTask task) {
        try {
            taskService.updateStatus(task.getId(), "RUNNING");

            String inputPath = task.getInputPath();
            File inputFile = new File(inputPath);
            if (!inputFile.isAbsolute()) {
                inputFile = new File(new File("").getAbsolutePath(), inputPath);
            }

            System.out.println("[INFO] 开始执行任务: " + task.getTaskName());
            System.out.println("[INFO] 输入文件: " + inputFile.getAbsolutePath());

            // 同时执行四种分析
            String[] types = {"RANK", "TIME_PEAK", "IP_DIST", "REGION"};
            int totalResults = 0;

            for (String type : types) {
                List<AnalysisResult> results = executeMapReduce(inputFile.getAbsolutePath(), type);
                if (results != null && !results.isEmpty()) {
                    // 为每种类型创建子任务记录
                    AnalysisTask subTask = taskService.createTask(
                            task.getUserId(),
                            task.getTaskName() + " - " + getTypeName(type),
                            type
                    );
                    taskService.updateStatus(subTask.getId(), "RUNNING");
                    taskService.saveResults(subTask.getId(), results);
                    taskService.updateStatus(subTask.getId(), "FINISHED");
                    totalResults += results.size();
                    System.out.println("[INFO] " + getTypeName(type) + " 完成，输出 " + results.size() + " 条结果");
                }
            }

            // 更新主任务状态
            taskService.updateStatus(task.getId(), "FINISHED");
            System.out.println("[INFO] 任务全部完成，共输出 " + totalResults + " 条结果");

        } catch (Exception e) {
            e.printStackTrace();
            taskService.updateStatus(task.getId(), "FAILED");
        }
    }

    private String getTypeName(String type) {
        switch (type) {
            case "RANK": return "访问量排行";
            case "TIME_PEAK": return "时间峰值";
            case "IP_DIST": return "IP分布";
            case "REGION": return "地区分布";
            default: return type;
        }
    }

    /**
     * 执行 MapReduce 分析
     */
    private List<AnalysisResult> executeMapReduce(String inputPath, String taskType) throws Exception {
        Map<String, Long> intermediate = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(inputPath), StandardCharsets.UTF_8))) {
            String line;
            boolean isHeader = true;
            while ((line = reader.readLine()) != null) {
                if (isHeader) { isHeader = false; continue; }
                String[] fields = line.split(",");
                if (fields.length < 4) continue;

                String ip = fields[0].trim();
                String region = fields[1].trim();
                String timestamp = fields[2].trim();
                String url = fields[3].trim();
                if (url.isEmpty()) continue;

                String mapperKey = null;
                switch (taskType) {
                    case "RANK":
                        mapperKey = url;
                        break;
                    case "TIME_PEAK":
                        if (timestamp.length() >= 13) {
                            mapperKey = url + "\t" + timestamp.substring(11, 13);
                        }
                        break;
                    case "IP_DIST":
                        mapperKey = url + "\t" + extractIpSegment(ip);
                        break;
                    case "REGION":
                        mapperKey = url + "\t" + region;
                        break;
                }

                if (mapperKey != null) {
                    intermediate.merge(mapperKey, 1L, Long::sum);
                }
            }
        }

        List<AnalysisResult> results = new ArrayList<>();
        for (Map.Entry<String, Long> entry : intermediate.entrySet()) {
            String[] keyParts = entry.getKey().split("\t");
            AnalysisResult result = new AnalysisResult();

            switch (taskType) {
                case "RANK":
                    result.setResultKey(keyParts[0]);
                    result.setResultValue(String.valueOf(entry.getValue()));
                    break;
                case "TIME_PEAK":
                case "IP_DIST":
                case "REGION":
                    result.setResultKey(keyParts[0]);
                    result.setExtra(keyParts.length > 1 ? keyParts[1] : "");
                    result.setResultValue(String.valueOf(entry.getValue()));
                    break;
            }
            results.add(result);
        }

        results.sort((a, b) -> Long.compare(
                Long.parseLong(b.getResultValue()),
                Long.parseLong(a.getResultValue())));

        return results;
    }

    private String extractIpSegment(String ip) {
        int dotCount = 0;
        for (int i = 0; i < ip.length(); i++) {
            if (ip.charAt(i) == '.') {
                dotCount++;
                if (dotCount == 2) return ip.substring(0, i);
            }
        }
        return ip;
    }
}
