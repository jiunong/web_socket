package com.xcloud.svg.socket;

import lombok.Builder;
import lombok.Data;
import lombok.With;

/**
 * TODO
 *
 * @author xuhong.ding
 * @since 2021/1/14 10:24
 */
@Data
@Builder
@With
public class DataPackage {

    private String id;
    private MsgStatus status;
    private String data;
    private Integer retryTime;
    private String onTime;
    private String offTime;

    public DataPackage retry(){
        this.retryTime++;
        return  this;
    }

}
