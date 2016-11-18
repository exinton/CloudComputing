package types;

import net.MsgType;

/**
 * Created by Yongtao on 9/20/2015.
 *
 * Extend your msg type like this.
 */
public enum ShareFileServerMsgType implements MsgType {

    GET_OSD_FILES, GET_FILE_OSDLIST, REPORT_NEW_FILE, REPORT_MOVE_FILE, REPLICATE, ERROR, REPLY
}
