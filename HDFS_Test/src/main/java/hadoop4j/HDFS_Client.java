package hadoop4j;

import hadoop4j.NoImplementionException;
import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.Seekable;

public class HDFS_Client {
	private Configuration conf;
	private String hdfs_path = "";
	public HDFS_Client() {
		conf = new Configuration();
		conf.addResource(new Path("conf/hadoop_conf/core-site.xml"));
		conf.addResource(new Path("conf/hadoop_conf/hdfs-site.xml"));
		conf.addResource(new Path("conf/hadoop_conf/mapred-site.xml"));
		hdfs_path = conf.get("fs.defaultFS");
	}
	public void create_file(String file_path, long size) throws Exception {
		if (!file_path.startsWith("/")) {
			System.out
					.println("Error: The input file path should start with /");
		}
		Path path = new Path(hdfs_path + file_path);
		FileSystem fs = FileSystem.get(conf);
		FSDataOutputStream out = fs.create(path, false);
		byte[] b = new byte[102400];
		while (size > 0) {
			if (size > 102400) {
				out.write(b);
				size = size - 102400;
			} else {
				b = new byte[(int) size];
				for (int i = 0; i < b.length; i++) {
					b[i] = 0;
				}
				out.write(b);
				size = 0;
			}
		}
		out.close();
	}

	public void create_dir(String file_path) throws Exception {
		if (!file_path.startsWith("/")) {
			System.out
					.println("Error: The input file path should start with /");
		}
		Path path = new Path(hdfs_path + file_path);
		FileSystem fs = FileSystem.get(conf);
		fs.mkdirs(path);
	}

	public void force_delete(String file_path) throws Exception {
		if (!file_path.startsWith("/")) {
			System.out
					.println("Error: The input file path should start with /");
		}
		Path path = new Path(hdfs_path + file_path);
		FileSystem fs = FileSystem.get(conf);
		fs.delete(path, true);
	}

	public void seq_read(String file_path, String offset_start,String offset_end) throws Exception {
		offset_start="0";
		Path path = new Path(hdfs_path + file_path);
		FileSystem fs = FileSystem.get(conf);
		FSDataInputStream in = fs.open(path);
        byte[] btbuffer = new byte[102400];
        in.seek(Long.valueOf(offset_start));  
        long size = Long.valueOf(offset_end) - Long.valueOf(offset_start);
        while(size>0){
        	if(size>=102400){
        		long current_pos = in.getPos();
        		in.read(btbuffer, 0, 102400);
        		in.seek(current_pos+102400);
        		size=size-102400;
        	}else{
        		in.read(btbuffer, 0, (int)size);
        		size=0;
        	}
        	
        }
	}
	public void seq_write(String file_path) throws Exception {
		new NoImplementionException().f();
	}

	public void ran_read(String file_path, String offset_start,String offset_end) throws Exception {
		Path path = new Path(hdfs_path + file_path);
		FileSystem fs = FileSystem.get(conf);
		FSDataInputStream in = fs.open(path);
        byte[] btbuffer = new byte[102400];
        in.seek(Long.valueOf(offset_start));  
        long size = Long.valueOf(offset_end) - Long.valueOf(offset_start);
        while(size>0){
        	if(size>=102400){
        		long current_pos = in.getPos();
        		in.read(btbuffer, 0, 102400);
        		in.seek(current_pos+102400);
        		size=size-102400;
        	}else{
        		in.read(btbuffer, 0, (int)size);
        		size=0;
        	}
        	
        }
	}

	public void ran_write(String file_path) throws Exception {
		new NoImplementionException().f();
	}

	public void append(String file_path, long size) throws Exception {
		if (!file_path.startsWith("/")) {
			System.out
					.println("Error: The input file path should start with /");
		}
		Path path = new Path(hdfs_path + file_path);
		FileSystem fs = FileSystem.get(conf);
		FSDataOutputStream out = fs.append(path);
		byte[] b = new byte[102400];
		while (size > 0) {
			if (size > 102400) {
				out.write(b);
				size = size - 102400;
			} else {
				b = new byte[(int) size];
				for (int i = 0; i < b.length; i++) {
					b[i] = 0;
				}
				out.write(b);
				size = 0;
			}
		}
		out.close();
	}

	public void ls(String file_path) throws Exception {
		Path f = new Path(file_path);
		FileSystem fs = FileSystem.get(conf);
		FileStatus[] status = fs.listStatus(f);
		for (int i = 0; i< status.length; i++) {
			 status[i].getPath().toString() ;
		}
	}

}
