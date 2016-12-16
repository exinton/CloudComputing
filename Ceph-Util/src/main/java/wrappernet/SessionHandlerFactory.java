package wrappernet;

import java.util.HashMap;
import java.util.Map;

import net.IOControl;
import net.MsgType;
import types.WrapperMsgType;
import wrapper.Wrapper;
import wrapper.WrapperHolder;

public class SessionHandlerFactory {
	
	 IOControl control;
	 WrapperHolder wrapper;
	 private static  SessionHandlerFactory instance = null;
	 static Map<WrapperMsgType,ISessionHandler> map =new HashMap<>();

		 

	
	public static  SessionHandlerFactory getSessionHandlerFactory(){
		if(instance==null){
			instance=new SessionHandlerFactory(); 
		}	
		return instance;
	}
	 
	
	public  void init(IOControl control,WrapperHolder wrapper){
		this.control=control;
		this.wrapper=wrapper;
		map.put(WrapperMsgType.HEARTBEAT, new HeartBeatSessionHandler(control,wrapper));
		map.put(WrapperMsgType.MULTI_CAST, new MULTICASTSessionHandler(control,wrapper));
		map.put(WrapperMsgType.FILE_VALID, new isFileValidHandler(control,wrapper));
	}
	
	public ISessionHandler getSessionHandler(MsgType type){
		return map.get(type);
	}
	
	

}
