/**
 * WordCount.java - the very-first MapReduce Program
 *
 * <h1>How To Compile</h1>
 * export HADOOP_HOME=/usr/lib/hadoop-0.20/
 * export DIR=wordcount_classes
 * rm -fR $DIR
 * mkdir $DIR
 * javac -classpath ${HADOOP_HOME}/hadoop-core.jar -d $DIR WordCount.java 
 * jar -cvf wordcount.jar -C $DIR .
 */

// package net.kzk9;

import java.io.IOException;
import java.util.StringTokenizer;

import java.util.HashMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class WordInDocs {
    public static class Map extends Mapper<LongWritable, Text, Text, IntWritable> {
        private Text word = new Text();
        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String line = value.toString();
		HashMap<String, Boolean> done =new HashMap<>();
		String[] temp =line.split("@@@@@");
		Text docID =new Text();
		docID.set(temp[0]);
		if(temp.length ==3) {
			line =temp[1] +" " +temp[2];
		}
		else if(temp.length ==4) {
			line =temp[1] +" " +temp[2] +" " +temp[3];
		}

            StringTokenizer tokenizer = new StringTokenizer(line);
            while (tokenizer.hasMoreTokens()) {
                Text token =new Text();
                token.set(tokenizer.nextToken());
                if(!done.containsKey(token.toString())) {
                    word.set(token);
                    context.write(word, docID);
                }
            }
        }
    }

    public static class Reduce extends Reducer<Text, Writable, Text, Writable> {
        private IntWritable value = new IntWritable(0);
        //@Override
        protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
		String result ="";
            for (Text value : values)
                result += value.get().toString() +"-";
		Text re =new Text();
		re.set(result);
            context.write(key, re);
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = new Job(conf, "wordcount");
        job.setJarByClass(WordCount.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        job.setMapperClass(Map.class);
        job.setReducerClass(Reduce.class);
        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
        job.setNumReduceTasks(1);

        FileInputFormat .setInputPaths(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        boolean success = job.waitForCompletion(true);
        System.out.println(success);
    }
}
