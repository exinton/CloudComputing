package wrappernet;


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import net.IOControl;
import net.Session;
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
	    ScheduledExecutorService scheduler= Executors.newScheduledThreadPool(1);		
	    scheduler.execute(new MultiCastBase(wrapper,control));
  
	    return true;
      }

}
