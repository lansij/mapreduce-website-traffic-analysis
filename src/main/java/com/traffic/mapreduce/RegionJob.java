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
 * 用户所在地区分布分析 MapReduce 作业
 * 统计每个地区的访问量分布
 *
 * 输入: CSV 文件 (ip, region, timestamp, url)
 * 输出: region \t count 或 url \t region \t count
 */
public class RegionJob {

    public static class RegionMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

        private static final IntWritable ONE = new IntWritable(1);
        private final Text keyOut = new Text();

        @Override
        protected void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {
            String line = value.toString();
            if (line.startsWith("ip") || line.startsWith("\"ip\"")) {
                return;
            }

            String[] fields = line.split(",");
            if (fields.length >= 4) {
                String region = fields[1].trim();
                String url = fields[3].trim();

                if (!region.isEmpty() && !url.isEmpty()) {
                    // 输出: url \t region
                    keyOut.set(url + "\t" + region);
                    context.write(keyOut, ONE);
                }
            }
        }
    }

    public static class RegionReducer extends Reducer<Text, IntWritable, Text, IntWritable> {

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

    public static boolean run(String inputPath, String outputPath, Configuration conf) throws Exception {
        Job job = Job.getInstance(conf, "Website Region Distribution Analysis");
        job.setJarByClass(RegionJob.class);
        job.setMapperClass(RegionMapper.class);
        job.setCombinerClass(RegionReducer.class);
        job.setReducerClass(RegionReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(job, new Path(inputPath));
        FileOutputFormat.setOutputPath(job, new Path(outputPath));
        return job.waitForCompletion(true);
    }
}
