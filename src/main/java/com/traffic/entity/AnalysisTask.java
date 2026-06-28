package com.traffic.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分析任务实体
 */
@Data
@TableName("t_task")
public class AnalysisTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String taskName;

    /** 任务类型: RANK / TIME_PEAK / IP_DIST */
    private String taskType;

    /** 状态: PENDING / RUNNING / FINISHED / FAILED */
    private String status;

    private String inputPath;

    private String outputPath;

    private LocalDateTime createTime;

    private LocalDateTime finishTime;
}
