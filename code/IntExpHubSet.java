//package neu.mr.project;

/**
 * Created by nikhilk on 12/4/15.
 */


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import java.io.*;
import java.util.HashSet;
import java.util.StringTokenizer;

public class IntExpHubSet {
    public static class FilterMap
            extends Mapper<Object, Text, Text, NullWritable> {
        HashSet<Integer> myset;
        private Text word = new Text();


        @Override
        protected void setup(Context context) throws IOException,
                InterruptedException {
            int num = 10;
            String mysting = "part-r-0000";
            myset = new HashSet<>();
            String line;

            for (int itr = 0 ;itr < num ; itr++ ){
                String myfile = mysting + itr;

                BufferedReader rdr = new BufferedReader(
                        new InputStreamReader(
                                new FileInputStream(
                                        new File(myfile))));


                // For each record in the user file
                while ((line = rdr.readLine()) != null) {
                    String [] split = line.split("\\s+");

                    for(int sitr=1; sitr < split.length; sitr++) {
                        myset.add(Integer.parseInt(split[sitr]));
                    }
                }
            }
        }

        public void map(Object key, Text value, Context context)
                throws IOException, InterruptedException {
        	StringTokenizer str = new StringTokenizer(value.toString());
        	String node = str.nextToken();
            //String[] split = value.toString().split("\\s+");

            if (myset.contains(Integer.parseInt(node))){
                word.set(node);
                context.write(word, NullWritable.get());
            }
        }
    }

    public static class FilterReduce extends Reducer<Text, NullWritable, Text, NullWritable> {
        LongWritable myvalue = new LongWritable();

        public void reduce(Text key, Iterable<NullWritable> values,
                           Context context
        ) throws IOException, InterruptedException {
            context.write(key, NullWritable.get());
        }
    }


    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
        if (otherArgs.length < 2) {
            System.err.println("Usage: wordcount <in> [<in>...] <out>");
            System.exit(2);
        }
        Job job = new Job(conf, "word count");
        job.setJarByClass(IntExpHubSet.class);
        job.setMapperClass(FilterMap.class);
        //job.setCombinerClass(IntSumReducer.class);
        job.setReducerClass(FilterReduce.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(NullWritable.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(NullWritable.class);
        job.setNumReduceTasks(10);
        for (int i = 0; i < otherArgs.length - 1; ++i) {
            FileInputFormat.addInputPath(job, new Path(otherArgs[i]));
        }
        FileOutputFormat.setOutputPath(job,
                new Path(otherArgs[otherArgs.length - 1]));


        //DistributedCache.addCacheFile(new Path(otherArgs[0]).toUri(),
        //job.getConfiguration());

        //DistributedCache.setLocalFiles(job.getConfiguration(), otherArgs[0]);
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }

}

