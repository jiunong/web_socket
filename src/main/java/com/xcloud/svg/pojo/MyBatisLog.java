package com.xcloud.svg.pojo;

import lombok.Data;

/**
 * TODO
 *
 * @author xuhong.ding
 * @since 2022/4/29 13:40
 */
@Data
public class MyBatisLog {
    private final static String SPLIT = "】-【";
    private String ip;

    private String userId;

    private String userName;

    private String dateTime;

    private String method;

    private String sql;

    private String sqlType;

    private String description;

    public MyBatisLog() {
    }

    public MyBatisLog(String content) {
        String[] split = content.split(SPLIT);
        this.ip = split[0];
        this.userId = split[1];
        this.userName = split[2];
        this.dateTime = split[3];
        this.method = split[4];
        this.sql = split[5];
        this.sqlType = split[6];
        this.description = split.length < 8 ? "" : split[7];
    }
}
