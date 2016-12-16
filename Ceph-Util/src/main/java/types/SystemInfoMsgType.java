package types;

import net.MsgType;

public enum SystemInfoMsgType implements MsgType{
		LIST_FILES,
		GET_READ_COUNTER,
		GET_WRITE_COUNTER,
		GET_READ_WRITE_LOAD,
		GET_SYSTEM_LOAD,
		GET_PROCESS_LOAD,
		GET_FREE_MEMORY,
		GET_FREE_SPACE,
		SYSTEM_INFO_ERROR,
		IS_ALIVE, FILE_EXIST,
		HEARTBEAT
}