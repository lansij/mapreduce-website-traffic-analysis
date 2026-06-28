package com.traffic.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 分析结果实体
 */
@Data
@TableName("t_result")
public class AnalysisResult {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;

    private String resultKey;

    private String resultValue;

    /** 额外信息 (地区、时段等) */
    private String extra;
}
