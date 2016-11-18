package osd;

import net.IOControl;
import net.MsgHandler;
import net.Session;


import types.FileReadMsgType;
//import sample.log.Utils;
import util.FileHelper;
import util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

/**
 * Created by Yongtao on 9/10/2015.
 * <p/>
 * This is demo for using IOControl as server.
 */
public class FileReadEchoServer{
	private static Log log=Log.get();

	/**
	 * Deal with echo and exit msg
	 */
//	static class Echo implements MsgHandler{
//		private IOControl control;
//		Echo(IOControl control){
//			this.control=control;
//		}
//		@Override
//		public boolean process(Session session) throws IOException{
//			control.response(new Session(EchoMsgType.ACK),session);
//			if(session.getType()==EchoMsgType.EXIT_SERVER)
//				control.quitServer();
//			return false;
//		}
//	}

	/**
	 * Handle file read.
	 */
	static class FileServer implements MsgHandler{
		private IOControl control;
		private String readDir;

		FileServer(IOControl control, String readDir){
			this.control=control;
			this.readDir = readDir;
		}

		@Override
		public boolean process(Session session) throws IOException{
			String path=readDir +File.separator+ session.getString("path");
			File file;			
			try{
				file=new File(path);
				FileInputStream fis=new FileInputStream(file);
				Session reply=new Session(FileReadMsgType.READ_FILE_OK);
				long fileSize=file.length();
				long position=session.getLong("position",0);
				long limit=session.getLong("limit",fileSize);
				if(limit>fileSize) limit=fileSize;
				reply.set("name",file.getName());
				reply.set("size",limit);
				reply.set("modify",file.lastModified());
				control.response(reply,session);
				SocketChannel channel=session.getSocketChannel();
				FileChannel fc=fis.getChannel();
				
				OSDGlobalParameters.incrementReadRequestConter(session.getString("path"));
				//  here is DMA copying utilizing sendfile system call.
				FileHelper.upload(fc,channel,limit,position);
				fis.close();
				FileReadWriteServer.checkOverLoaded(control);
			}catch(Exception e){
				log.w(e);
				Session error = null;
				if(OSDGlobalParameters.getThisNode().isOverloaded(session.getString("path")))
					error=new Session(FileReadMsgType.FILE_OVERLOADED);
				else
					error=new Session(FileReadMsgType.READ_FILE_ERROR);
				error.set("comment",e.getMessage());
				control.response(error,session);
			}
			return false;
		}
	}

	public static void main(String args[]){
            final  String CEPH_HOME = System.getenv("CEPH_HOME");
            
            OSDProperty osd = OSDProperty.getInstance(CEPH_HOME);
            try{
                IOControl server=new IOControl();
                //  register echo handlers
//                MsgHandler logger=new Echo(server);
//                server.registerMsgHandlerLast(logger,new EchoMsgType[]{EchoMsgType.ECHO,EchoMsgType.EXIT_SERVER});
//                
//                // register filters
//               	MsgFilter stat=new RawLogger();
//                server.registerMsgFilterHead(stat);
                
                // register file read handler
                MsgHandler fileRead=new FileServer(server,osd.getCEPH_DATA_DIR());
                server.registerMsgHandlerHead(fileRead,FileReadMsgType.READ_FILE);
                // start server
                server.startServer(osd.getREAD_SERVER_PORT());
                System.out.println("Starting Read Server !!!");
                // blocking until asked to quit (see SimpleEchoClient)
                server.waitForServer();
            }catch(IOException e){
                log.w(e);
            }
        }
}
