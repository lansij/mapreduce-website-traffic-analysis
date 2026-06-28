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
 * 网站访问量热度排行 MapReduce 作业
 * 统计每个网站的总访问量，按访问量降序排列
 *
 * 输入: CSV 文件 (ip, region, timestamp, url)
 * 输出: url \t count
 */
public class TrafficRankJob {

    /**
     * Mapper: 读取每行 CSV，提取 URL，输出 <url, 1>
     */
    public static class RankMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

        private static final IntWritable ONE = new IntWritable(1);
        private final Text urlOut = new Text();

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
                String url = fields[3].trim();
                if (!url.isEmpty()) {
                    urlOut.set(url);
                    context.write(urlOut, ONE);
                }
            }
        }
    }

    /**
     * Reducer: 汇总每个 URL 的访问总量
     */
    public static class RankReducer extends Reducer<Text, IntWritable, Text, IntWritable> {

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
        Job job = Job.getInstance(conf, "Website Traffic Rank Analysis");
        job.setJarByClass(TrafficRankJob.class);
        job.setMapperClass(RankMapper.class);
        job.setCombinerClass(RankReducer.class);
        job.setReducerClass(RankReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(job, new Path(inputPath));
        FileOutputFormat.setOutputPath(job, new Path(outputPath));
        return job.waitForCompletion(true);
    }
}
