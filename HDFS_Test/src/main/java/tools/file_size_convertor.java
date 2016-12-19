package tools;

import java.text.DecimalFormat;

public class file_size_convertor {
	public String readable_file_size(long size){
		DecimalFormat df=new DecimalFormat("#.###");   
		if(size<1024){
			return (df.format(size)+" Bytes");
		}else if(size<(1024*1024)){
			return (df.format(size/1024)+ " KB");
		}else if(size<1024*1024*1024){
			return (df.format(size/1024/1024)+ " MB");	
		}else{
			return (df.format(size/1024/1024/1024)+ " GB");	
		}
	}	
	
	public String readable_file_size(double size){
		DecimalFormat df=new DecimalFormat("#.###");   
		if(size<1024){
			return (df.format(size)+" Bytes");
		}else if(size<(1024*1024)){
			return (df.format(size/1024)+ " KB");
		}else if(size<1024*1024*1024){
			return (df.format(size/1024/1024)+ " MB");	
		}else{
			return (df.format(size/1024/1024/1024)+ " GB");	
		}
	}
}
