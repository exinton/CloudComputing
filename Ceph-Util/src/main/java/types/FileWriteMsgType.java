package types;

import net.MsgType;

public enum FileWriteMsgType implements MsgType{
	//  Client to primary
	WRITE_CHUNK,   //  id,size,timeout,address
	WRITE_OK,WRITE_FAIL,
	//  Primary and secondary
	WRITE_CHUNK_CACHE, //  id,size,timeout,address,start,transid,primary
	COMMIT_OK,COMMIT_FAIL,   //  transid
	TRANSFER_FILE,
	TRANSFER_OK,
	TRANSFER_FAIL,
	DELETE_FILE,
	DELETE_OK,
	DELETE_ERROR
}
