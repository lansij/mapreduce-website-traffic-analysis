-- 网站访问量统计分析系统 数据库初始化脚本

CREATE DATABASE IF NOT EXISTS traffic_analysis DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;
USE traffic_analysis;

-- 用户表
CREATE TABLE IF NOT EXISTS t_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色: USER/ADMIN',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 分析任务表
CREATE TABLE IF NOT EXISTS t_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '提交用户ID',
    task_name VARCHAR(100) NOT NULL COMMENT '任务名称',
    task_type VARCHAR(50) NOT NULL COMMENT '任务类型: RANK/TIME_PEAK/IP_DIST',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING/RUNNING/FINISHED/FAILED',
    input_path VARCHAR(500) COMMENT '输入数据路径',
    output_path VARCHAR(500) COMMENT '输出结果路径',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    finish_time DATETIME COMMENT '完成时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分析任务表';

-- 分析结果表
CREATE TABLE IF NOT EXISTS t_result (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id BIGINT NOT NULL COMMENT '关联任务ID',
    result_key VARCHAR(200) COMMENT '结果键',
    result_value VARCHAR(200) COMMENT '结果值',
    extra VARCHAR(500) COMMENT '额外信息(如地区、时段等)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分析结果表';

-- 默认管理员账号
INSERT INTO t_user (username, password, role) VALUES ('admin', 'admin123', 'ADMIN');
-- 测试用户
INSERT INTO t_user (username, password, role) VALUES ('test', 'test123', 'USER');
