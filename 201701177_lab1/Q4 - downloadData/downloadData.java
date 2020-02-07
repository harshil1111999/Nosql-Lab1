package downloadData;

import java.io.IOException;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.fs.Path;

public class downloadData 
{
	public static class downloadMapper extends Mapper<LongWritable,Text,Text,IntWritable>
	{
		public void map(LongWritable key, Text value, Context context) throws IOException,InterruptedException 
		{
			String line = value.toString();
			
			int i=0;
			while(i<line.length() && line.charAt(i)!='[')
			{
				i++;
			}
			i+=4;
			String month=String.valueOf(line.charAt(i))+String.valueOf(line.charAt(i+1))+String.valueOf(line.charAt(i+2));
			i+=4;
			String year=String.valueOf(line.charAt(i))+String.valueOf(line.charAt(i+1))+String.valueOf(line.charAt(i+2))+String.valueOf(line.charAt(i+3));
			
			value.set(month+"-"+year);
			while(i<line.length() && line.charAt(i)!='"')
			{
				i++;
			}
			i++;
			while(i<line.length() && line.charAt(i)!='"')
			{
				i++;
			}
		
			i++;
			
			while(i<line.length() && line.charAt(i)!=' ')
			{
				i++;
			}
			
			i+=2;
			
			while(i<line.length() && line.charAt(i)!=' ')
			{
				i++;
			}
			
			String size_str="";
			int file_size=0;
			i++;
			
			if(line.charAt(i)!='-')
			{
				while(i<line.length() && line.charAt(i)!=' ')
				{
					size_str+=line.charAt(i);
					i++;
				}
				file_size=Integer.parseInt(size_str);
			}
			
			context.write(value ,new IntWritable(file_size));
		}
	}
	
	public static class downloadReducer extends Reducer<Text,IntWritable,Text,IntWritable> 
	{
		public void reduce(Text key, Iterable<IntWritable> values,Context context) throws IOException,InterruptedException 
		{
			int total=0;
			int count=0;
			for(IntWritable it: values)
			{
				total+=it.get();
				count++;
			}
			
			String str_count = Integer.toString(count);
			
			String nkey = key.toString();
			key.set(nkey+" "+str_count);
			context.write(key, new IntWritable(total));
		}
	}
	
	public static void main(String[] args) throws Exception 
	{
		Configuration conf= new Configuration();
		Job job = Job.getInstance(conf,"dataSize");
		job.setJarByClass(downloadData.class);
		
		job.setMapperClass(downloadMapper.class);
		job.setReducerClass(downloadReducer.class);
		
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		
		Path outputPath = new Path(args[1]);
		//Configuring the input/output path from the filesystem into the job
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		
		//deleting the output path automatically from hdfs so that we don't have to delete it explicitly
		outputPath.getFileSystem(conf).delete(outputPath,true);
		//exiting the job only if the flag value becomes false
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}

}
