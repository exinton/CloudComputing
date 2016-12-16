package wrappernet;

import java.sql.Wrapper;
import java.util.List;

import net.Address;
import net.IOControl;
import net.Session;
import types.WrapperMsgType;
import wrapper.WrapperHolder;
import wrapper.WrapperUtils;

public class isFileValidHandler implements ISessionHandler {
	
	IOControl control;
	WrapperHolder wrapper;
	public isFileValidHandler(IOControl control,WrapperHolder wrapper){
		
		this.control=control;
		this.wrapper=wrapper;
	}
	
	@Override
	public boolean handling(Session session) {
		long selfEpochVal=wrapper.getWrapper().getEpochVal();
		 Long epochVal = session.getLong("epochVal");      
		 Session reply = new Session(WrapperMsgType.FILE_VALID);
		 
		 if(epochVal==selfEpochVal){
			 reply.set("isFileValid", true);
			 try {
				control.response(reply, session);
				return true;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return true;
			}
		 }		
		 reply.set("epochVal",selfEpochVal );
		 reply.set("isFileValid", false);
		 try {
			 control.response(reply, session);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
		 
		 
	}
	


}
