package wrappernet;

import java.io.IOException;

import net.IOControl;
import net.MsgHandler;
import net.MsgType;
import net.Session;
import types.WrapperMsgType;
import wrapper.Wrapper;
import wrapper.WrapperHolder;

public class WrapperMsgHandler implements MsgHandler{



     private IOControl control;
     private WrapperHolder wrapper;

     public WrapperMsgHandler(IOControl control,WrapperHolder wrapper) {
         this.control = control;
         this.wrapper=wrapper;
         SessionHandlerFactory.getSessionHandlerFactory().init(control, wrapper);
     }

     @Override
     public boolean process(Session session) throws IOException {

         System.out.println("Starting processing " + session.getType());
         MsgType type = session.getType();

         ISessionHandler sessionhandler = SessionHandlerFactory.getSessionHandlerFactory().getSessionHandler(type);
         if(sessionhandler==null)
        	 return false;
         else
        	 return sessionhandler.handling(session);

     }



}