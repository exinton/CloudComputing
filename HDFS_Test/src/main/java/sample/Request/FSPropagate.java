package sample.Request;

import req.Rand.RandomGenerator;
import req.Rand.UniformGenerator;
import req.Request;
import req.RequestCallback;
import req.StaticTree;
import tools.log_writer;

import hadoop4j.RequestAdapterHDFSAPI;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import api.RequestAdapterAPI;

public class FSPropagate{
	public static String printLine(List list){
		String lstring=list.toString();
		return lstring.substring(1,lstring.length()-1)+"\n";
	}

	public static void parse(String input,String output,RequestCallback call) throws IOException{
		try(Writer out=new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(output),"UTF-8"))){
			StaticTree tree=StaticTree.getStaticTree(input,new UniformGenerator());
			for(int i=0;i<tree.getFileSize();++i){
				Request req=tree.fileInfo(i);
				req.type=Request.ReqType.CREATE_FILE;
				out.write(printLine(call.call(req)));
			}
		}
	}

	public static class NullCall implements RequestCallback{
		List<Integer> order=new ArrayList<>();
		RandomGenerator uniform=new UniformGenerator();
		String server_type;
		log_writer lw  ;
		RequestAdapterAPI api; 
		public NullCall(String server_type,log_writer lw){
			for(int i=1;i<10;++i) order.add(i);
			this.server_type=server_type;
			this.lw=lw;
			
			if(this.server_type.equals("HDFS")){
			this.api= new RequestAdapterHDFSAPI(lw);
			}else if(this.server_type=="Ceph"){
				
			}else if(this.server_type=="Swift"){
			}
		}

		@Override
		public List<Integer> call(Request request){
			StaticTree.plainShuffle(order,uniform);
			int find=uniform.nextInt(6)+1; //  1~6
			System.out.println(request+" : "+find);
			api.transfer(request.toString());
			return order.subList(0,find);
		}
	}

	public static void run_fspropagate_generator(String server_type,String request_log,String source_file)  throws IOException{
		log_writer lw =new log_writer(request_log);
		parse(source_file,"files/rank.txt",new NullCall(server_type,lw));
		lw.close_log_writer();
	}
}
