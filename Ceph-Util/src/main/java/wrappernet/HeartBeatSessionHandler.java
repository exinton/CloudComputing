package wrappernet;

import java.io.IOException;

import net.IOControl;
import net.Session;
import types.WrapperMsgType;
import wrapper.WrapperHolder;
import wrapper.WrapperHolderType;
import wrapper.WrapperUtils;

public class HeartBeatSessionHandler implements ISessionHandler{
	long selfEpochVal;
	IOControl control;
	WrapperHolder wrapper;
	public HeartBeatSessionHandler(IOControl control,WrapperHolder wrapper){
		this.selfEpochVal=wrapper.getWrapper().getEpochVal();
		this.control=control;
		this.wrapper=wrapper;
	}
	@Override
	public boolean handling(Session session) {
	 
       Long epochVal = session.getLong("epochVal");       
       System.out.println("received heart beat with wrapper version"+epochVal);
       if (selfEpochVal>epochVal) {	//remote is outdate
    	   Session reply = new Session(WrapperMsgType.HEARTBEAT);
           reply.set("isValid", false);     
           String outJson = WrapperUtils.saveToString(wrapper.getWrapper()); 
           reply.set("latestMap", outJson);
           try {
			control.response(reply, session);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}                        
       } else if(selfEpochVal<epochVal && wrapper.getType()!=WrapperHolderType.MONITOR) {	//self is outdate
    	   	System.out.println("receiving higher version wrapper");
    	   	if(session.getHeaderMap().containsKey("latestMap")){
    	   		String jsonValue = session.getString("latestMap");
    	   		wrapper.setWrapper(WrapperUtils.loadFromString(jsonValue));	
    	   	}else{
    	   		wrapper.setWrapper(WrapperUtils.downloadWrapper());
    	   		System.out.println("upgrade current version"+selfEpochVal+" to "+wrapper.getWrapper().getEpochVal());
    	        WrapperUtils.saveToJSON(wrapper.getJsonFile(), wrapper.getWrapper());
    	   	}
    	   	
       }else{
    	   Session reply = new Session(WrapperMsgType.HEARTBEAT);
           reply.set("isValid", true);                      
           try {
			control.response(reply, session);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
       }
       return true;
      }

}

