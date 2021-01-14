package com.xcloud.svg.socket;

/**
 * TODO
 *
 * @author xuhong.ding
 * @since 2021/1/14 14:26
 */
public enum MsgStatus {

    ALIVE(1,"待消费");

    private Integer code;
    private String status;
    MsgStatus() {
    }
    MsgStatus(Integer code, String status) {
        this.code = code;
        this.status = status;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
