package client;

import net.IOControl;
import net.Session;
import req.Rand.ContentSrc;
import sample.log.Utils;
import util.FileHelper;
import util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Scanner;

import types.FileWriteMsgType;
import net.Address;

public class FileWriteClient{
	private static final Log log=Log.get();
	static long timeout=60*1000;    //  60 seconds

	static boolean upload(IOControl control,String path,ArrayList<Address> addresses,long size, boolean test){
		try{
			Session req=new Session(FileWriteMsgType.WRITE_CHUNK);
                        if(!test){
                            File file=new File(path);
                            FileInputStream fis=new FileInputStream(file);
                            FileChannel src=fis.getChannel();			
                            size = file.length();
                            String id = file.getName();
                            req.set("id",id);
                            req.set("size",size);
                            req.set("timeout",timeout);
                            req.set("address",addresses);
                            control.send(req,addresses.get(0).getIp(),addresses.get(0).getPort());
                            SocketChannel dest=req.getSocketChannel();
                            FileHelper.upload(src,dest,size);
                            fis.close();
                        }
                        else{
                            String id=path;//file.getName();
                            if(size>4096)
                                size = 4096;
                            req.set("id",id);
                            req.set("size",size);
                            req.set("timeout",timeout);
                            req.set("address",addresses);
                            control.send(req,addresses.get(0).getIp(),addresses.get(0).getPort());
                            SocketChannel dest=req.getSocketChannel();
                            ContentSrc genSrc=new ContentSrc("Test me baby.");
                            FileHelper.upload(genSrc, dest, size);
                            genSrc.close();
			}
			Session result=control.get(req);
                        System.out.println(result.getType());
			return result.getType()==FileWriteMsgType.WRITE_OK;
		}catch(Exception e){
			log.w(e);
			return false;
		}
	}
	static boolean upload(IOControl control,String path,ArrayList<Address> addresses){
                return upload(control,path,addresses,0,false);
	}
	static ArrayList<Address> splitAddress(String[] tokens,int start){
		ArrayList<Address> result=new ArrayList<>();
		for(int i=start;i<tokens.length;++i){
			String[] parts=tokens[i].split(":");
			if(parts.length!=2) return null;
			try{
				int port=Integer.parseInt(parts[1]);
				Address address=new Address(parts[0],port);
				result.add(address);
			}catch(NumberFormatException e){return null;}
		}
		return result;
	}

	public static void FileWrite(IOControl control,String file,ArrayList<Address> addresses,Long size, boolean test){
		
			if(addresses!=null){
				if(upload(control,file,addresses,size,test))
					log.i("File upload success.");
				else
					log.i("File upload fails.");
			}
		
	}
	
	public static void main(String args[]){
		try{
			Utils.connectToLogServer(log);
			try{
				IOControl control=new IOControl();
				//  get what you type
				Scanner in=new Scanner(System.in);
				for(;;){
					String cmd=in.nextLine();
					if(cmd.length()>0){
						String line=cmd.trim();
						String[] tokens=line.split("\\s");
						if(tokens.length>1){
							ArrayList<Address> addresses=splitAddress(tokens,1);
							if(addresses!=null){
								if(upload(control,tokens[0],addresses))
									log.i("File upload success.");
								else
									log.i("File upload fails.");
								continue;
							}
						}
						log.i("Input local file name and list of servers");
					}
				}
			}catch(Exception e){
				log.w(e);
			}
		}catch(IOException e){
			log.w(e);
		}
	}
}
