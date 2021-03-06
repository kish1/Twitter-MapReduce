/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
//package org.apache.hadoop.examples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

public class Outlinks {

  public static class TokenizerMapper 
       extends Mapper<Object, Text, IntWritable, IntWritable>{
    
    private static IntWritable mkey = new IntWritable();
    private static IntWritable mval = new IntWritable();
      
    public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {
      //ArrayList<String> details = CSVParser.parseLine(value.toString());
      String[] parts = value.toString().split("[ ,]");
      mkey.set(Integer.parseInt(parts[0]));
      mval.set(Integer.parseInt(parts[1]));
     
      context.write(mkey, mval);
    }
  }
  
  public static class IdentityReducer 
       extends Reducer<IntWritable,IntWritable,IntWritable,Text> {
	 /* 
	HashMap<Integer, ArrayList<Integer>> map;
	protected void setup(Context context)
	{
		map = new HashMap<Integer, ArrayList<Integer>>();
	}
    */
	Text value = new Text();
	int NVal = 50;
    public void reduce(IntWritable key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException  {
      StringBuilder sb = new StringBuilder("");
      int count = 0;
      for (IntWritable val : values) {
    	  sb.append(Integer.toString(val.get()) + " ");
    	  count++;
      }
      if(count >= NVal)
      {
    	  value.set(sb.toString());
    	  context.write(key, value);
      }
    }
      /*
     protected void cleanup(Context context) throws IOException, InterruptedException
     {
    	 StringBuilder sb;
    	 IntWritable rkey = new IntWritable();
    	 Text rval = new Text();
    	 for(Map.Entry<Integer, ArrayList<Integer>> entry : map.entrySet())
    	 {
    		 sb = new StringBuilder("");
    		 for(Integer val : (ArrayList<Integer>)entry.getValue())
    			 sb.append(val.toString() + " ");
    		 rkey.set(entry.getKey());
    		 rval.set(sb.toString());
    		 context.write(rkey, rval);
    	 }
    	 
     }*/
      
  }

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
    if (otherArgs.length < 2) {
      System.err.println("Usage: inlinks <in> [<in>...] <out>");
      System.exit(2);
    }
    Job job = new Job(conf, "inlinks");
    job.setJarByClass(Outlinks.class);
    job.setMapperClass(TokenizerMapper.class);
    //job.setCombinerClass(IdentityReducer.class);
    job.setReducerClass(IdentityReducer.class);
    job.setNumReduceTasks(10);
    job.setOutputKeyClass(IntWritable.class);
    job.setOutputValueClass(IntWritable.class);
    for (int i = 0; i < otherArgs.length - 1; ++i) {
      FileInputFormat.addInputPath(job, new Path(otherArgs[i]));
    }
    FileOutputFormat.setOutputPath(job,
      new Path(otherArgs[otherArgs.length - 1]));
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}
