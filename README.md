# MapReduce 网站访问量统计分析系统

基于 Hadoop MapReduce 的网站访问量统计分析系统，使用 Spring Boot 3.2.5 构建。

## 技术栈

- **后端框架：** Spring Boot 3.2.5
- **大数据处理：** Hadoop MapReduce
- **开发语言：** Java 17
- **构建工具：** Maven
- **前端：** HTML + CSS + JavaScript + ECharts

## 项目结构

```
├── data/                          # 测试数据
│   └── access_log.csv             # 网站访问日志
├── src/main/java/com/traffic/
│   ├── config/                    # 配置类
│   ├── controller/                # 控制器
│   ├── entity/                    # 实体类
│   ├── mapper/                    # 数据访问层
│   ├── mapreduce/                 # MapReduce 任务
│   │   ├── IpDistributionJob.java # IP 分布统计
│   │   ├── RegionJob.java         # 地域分析
│   │   ├── TimePeakJob.java       # 访问高峰分析
│   │   └── TrafficRankJob.java    # 流量排行
│   └── service/                   # 业务逻辑层
└── src/main/resources/
    ├── application.yml            # 应用配置
    ├── schema.sql                 # 数据库建表脚本
    └── static/                    # 前端页面
```

## 功能模块

| 模块 | 说明 |
|------|------|
| 流量排行 | 统计各页面访问量排名 |
| IP 分布 | 分析访客 IP 地址分布 |
| 地域分析 | 按地区统计访问来源 |
| 高峰分析 | 识别访问量高峰时段 |

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- Hadoop 3.x
- MySQL 8.0+

### 启动步骤

1. 初始化数据库
   ```bash
   mysql -u root -p < src/main/resources/schema.sql
   ```

2. 修改数据库连接配置（`application.yml`）

3. 启动应用
   ```bash
   mvn spring-boot:run
   ```

4. 访问系统
   - 首页：`http://localhost:8080`
   - 管理后台：`http://localhost:8080/admin`
