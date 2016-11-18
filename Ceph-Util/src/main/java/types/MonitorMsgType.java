package types;

import net.MsgType;

/**
 * Created by Avinash on 10/16/2015.
 * <p/>
 * Monitor message types 
 */
public enum MonitorMsgType implements MsgType{
	MODIFY_MAP_ADD,  //  path, [position], [limit]
	MODIFY_MAP_REMOVE,
        INITIATE_LOAD_BALANCING, // removed MODIFY_MAP_OVERLOADED since it has become obsolete
        ACK_MODIFY,
	START_LOADBALANCE,
        MODIFY_ERROR,
        CACHE_VALID,
        CACHE_GET, 
        MONITOR_ERROR,
        UPDATE_MAP,
        UPDATE_MAP_RESPONSE,
        OVERLOADED,
        NODE_FAIL,
        OSD_OVERLOADED
        
        
}
