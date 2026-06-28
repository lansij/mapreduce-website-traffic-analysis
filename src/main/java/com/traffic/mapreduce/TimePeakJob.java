package com.traffic.mapreduce;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

/**
 * 时间段流量峰值分析 MapReduce 作业
 * 按网站和小时分组统计访问量，找出各网站的流量峰值时段
 *
 * 输入: CSV 文件 (ip, region, timestamp, url)
 * 输出: url \t hour \t count
 */
public class TimePeakJob {

    /**
     * Mapper: 解析 timestamp，提取小时，输出 <url_hour, 1>
     */
    public static class PeakMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

        private static final IntWritable ONE = new IntWritable(1);
        private final Text keyOut = new Text();

        @Override
        protected void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {
            String line = value.toString();
            // 跳过表头
            if (line.startsWith("ip") || line.startsWith("\"ip\"")) {
                return;
            }

            String[] fields = line.split(",");
            if (fields.length >= 4) {
                String timestamp = fields[2].trim();
                String url = fields[3].trim();

                // 提取小时: "2024-01-15 08:23:45" -> "08"
                if (timestamp.length() >= 13 && !url.isEmpty()) {
                    String hour = timestamp.substring(11, 13);
                    keyOut.set(url + "\t" + hour);
                    context.write(keyOut, ONE);
                }
            }
        }
    }

    /**
     * Reducer: 汇总每个 网站+小时 的访问量
     */
    public static class PeakReducer extends Reducer<Text, IntWritable, Text, IntWritable> {

        private final IntWritable result = new IntWritable();

        @Override
        protected void reduce(Text key, Iterable<IntWritable> values, Context context)
                throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
            result.set(sum);
            context.write(key, result);
        }
    }

    /**
     * 运行任务
     */
    public static boolean run(String inputPath, String outputPath, Configuration conf) throws Exception {
        Job job = Job.getInstance(conf, "Website Time Peak Analysis");
        job.setJarByClass(TimePeakJob.class);
        job.setMapperClass(PeakMapper.class);
        job.setCombinerClass(PeakReducer.class);
        job.setReducerClass(PeakReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(job, new Path(inputPath));
        FileOutputFormat.setOutputPath(job, new Path(outputPath));
        return job.waitForCompletion(true);
    }
}
