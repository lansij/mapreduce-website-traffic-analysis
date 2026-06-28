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
 * IP 来源分布分析 MapReduce 作业
 * 统计每个网站的访问来源 IP 分布情况
 *
 * 输入: CSV 文件 (ip, region, timestamp, url)
 * 输出: url \t ip \t count
 */
public class IpDistributionJob {

    /**
     * Mapper: 输出 <url_ip, 1>
     */
    public static class IpMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

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
                String ip = fields[0].trim();
                String url = fields[3].trim();

                if (!ip.isEmpty() && !url.isEmpty()) {
                    // 将 IP 转为网段 (保留前两段)
                    String ipSegment = ip;
                    int dotCount = 0;
                    for (int i = 0; i < ip.length(); i++) {
                        if (ip.charAt(i) == '.') {
                            dotCount++;
                            if (dotCount == 2) {
                                ipSegment = ip.substring(0, i);
                                break;
                            }
                        }
                    }
                    keyOut.set(url + "\t" + ipSegment);
                    context.write(keyOut, ONE);
                }
            }
        }
    }

    /**
     * Reducer: 汇总每个 网站+IP段 的访问量
     */
    public static class IpReducer extends Reducer<Text, IntWritable, Text, IntWritable> {

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
        Job job = Job.getInstance(conf, "Website IP Distribution Analysis");
        job.setJarByClass(IpDistributionJob.class);
        job.setMapperClass(IpMapper.class);
        job.setCombinerClass(IpReducer.class);
        job.setReducerClass(IpReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(job, new Path(inputPath));
        FileOutputFormat.setOutputPath(job, new Path(outputPath));
        return job.waitForCompletion(true);
    }
}
