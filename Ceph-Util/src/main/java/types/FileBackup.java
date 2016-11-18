package types;

import net.MsgType;

public enum FileBackup implements MsgType {
	FILE_ADD,
	FILE_DELETE,
	TRANSFER_FILE_LIST,
	FILE_OVERLOADED,
	GET_PREV_NEXT
}
