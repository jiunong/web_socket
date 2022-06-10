package com.xcloud.svg.util;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;
import com.xcloud.svg.socket.DataPackage;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.Session;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;


/**
 * TODO
 *
 * @author xuhong.ding
 * @since 2021/1/15 14:51
 */
public class LogsHandler {


    private static final String logPath = FileUtil.getWebRoot().getPath().concat(FileUtil.isWindows() ? "\\logs\\socket_log.out" : "/logs/socket_log.out");

    private static final File logOutFile;
    private static LogsHandler logsHandler;

    static {
        logOutFile = new File(logPath);
        if (!FileUtil.exist(logOutFile)) {
            FileUtil.touch(logOutFile);
        }
    }

    public static LogsHandler instance() {
        if (logsHandler == null) {
            logsHandler = new LogsHandler();
        }
        return logsHandler;
    }

    /**
     * TODO 记录消息
     *
     * @param session      客户端
     * @param dataPackages 数据包
     * @author xuhong.ding
     * @since 2021/1/15 15:06
     */
    public static void logs(Session session, DataPackage dataPackages) {
        List<String> logs = new ArrayList<>();
        logs.add(DateTime.now().toString()
                .concat(":")
                .concat(SocketAddress.getRemoteAddress(session))
                .concat(":")
                .concat(JSONUtil.parse(dataPackages).toString()));
        FileUtil.appendUtf8Lines(logs, logOutFile);
    }

    /**
     * TODO 添加日志
     *
     * @param log
     * @author xuhong.ding
     * @since 2021/1/19 10:04
     */
    public static void logs(String log) {
        FileUtil.appendUtf8Lines(ListUtil.toList(DateTime.now().toString()
                .concat(":")
                .concat(log)), logOutFile);
    }

    /**
     * TODO 添加日志
     *
     * @param log 日志
     * @author xuhong.ding
     * @since 2021/1/19 10:04
     */
    public void logs(String log, Object... var2) {
        AtomicReference<String> logNew = new AtomicReference<>(log);

        Arrays.asList(var2).forEach(u -> logNew.set(logNew.get().replaceFirst("\\{}", (String) u)));
        FileUtil.appendUtf8Lines(ListUtil.toList(DateTime.now().toString()
                .concat(":[")
                .concat(logNew.get())
                .concat("]")), logOutFile);
    }

}
