package wrappernet;


import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import net.IOControl;
import net.Session;
import types.MonitorMsgType;
import types.WrapperMsgType;
import wrapper.WrapperHolder;


public class MULTICASTSessionHandler implements ISessionHandler {
	long selfEpochVal;
	IOControl control;
	WrapperHolder wrapper;
	public MULTICASTSessionHandler(IOControl control,WrapperHolder wrapper){
		this.selfEpochVal=wrapper.getWrapper().getEpochVal();
		this.control=control;
		this.wrapper=wrapper;
	}
	
	
	@Override
	public boolean handling(Session session) {	
	    new MultiCastBase(wrapper,control).run();
	    Session reply = new Session(WrapperMsgType.MULTI_CAST);
	    try {
			control.response(reply, session);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return true;
	    
      }

}
