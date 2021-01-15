package com.xcloud.svg.util;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;
import com.xcloud.svg.socket.DataPackage;

import javax.websocket.Session;
import java.io.File;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 *
 * @author xuhong.ding
 * @since 2021/1/15 14:51
 */
public class LogsHandler {


    private static final String logPath = FileUtil.getWebRoot().getPath().concat(FileUtil.isWindows() ? "\\logs\\socket_log.out" : "/logs/socket_log.out");


    /**
     * TODO
     *
     * @param session      客户端
     * @param dataPackages 数据包
     * @author xuhong.ding
     * @since 2021/1/15 15:06
     */
    public static void logs(Session session, DataPackage dataPackages) {
        InetSocketAddress remoteAddress = SocketAddress.getRemoteAddress(session);
        List<String> logs = new ArrayList<>();
        logs.add(DateTime.now().toString()
                .concat(":")
                .concat(remoteAddress.toString())
                .concat(":")
                .concat(JSONUtil.parse(dataPackages).toString()));
        File logOutFile = new File(logPath);
        if (!FileUtil.exist(logOutFile)) {
            FileUtil.touch(logOutFile);
        }
        FileUtil.appendUtf8Lines(logs, logOutFile);
    }


}
