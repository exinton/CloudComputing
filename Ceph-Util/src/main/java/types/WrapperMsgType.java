package types;

import net.MsgType;


public enum WrapperMsgType implements MsgType{
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
        OSD_OVERLOADED,
        HEARTBEAT,
        MULTI_CAST,
        FILE_VALID
 
}
