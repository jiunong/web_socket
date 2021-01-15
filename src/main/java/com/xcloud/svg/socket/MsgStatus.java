package com.xcloud.svg.socket;

/**
 * TODO
 *
 * @author xuhong.ding
 * @since 2021/1/14 14:26
 */
public enum MsgStatus {

    ALIVE("待消费"),
    DEAL("已消费"),
    RETRY("重试"),
    FAIL("未消费");
    MsgStatus( String status) {
    }

}
