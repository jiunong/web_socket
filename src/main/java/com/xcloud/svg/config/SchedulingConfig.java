package com.xcloud.svg.config;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.xcloud.svg.socket.DataPackage;
import com.xcloud.svg.socket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * TODO 配置定时任务
 *
 * @author xuhong.ding
 * @since 2021/1/14 13:41
 */
@Slf4j
@Component
@EnableScheduling
public class SchedulingConfig {

    @Scheduled(cron = "0/30 * * * * ?")
    public void checkMsgStream() {
        HashMap<Session, List<DataPackage>> msgStream = WebSocketServer.getMsgStream();
        msgStream.forEach((k, v) -> {
            List<DataPackage> dataPackages = new LinkedList<>();
            JSONArray objects = JSONUtil.parseArray(v);
            objects.forEach(u -> {
                DataPackage dataPackage = Optional.of(u).map(m -> JSONUtil.toBean((JSONObject) m, DataPackage.class)).map(DataPackage::retry).get();
                if(dataPackage.getRetryTime() < 6){
                    dataPackages.add(dataPackage);
                }
            });
            WebSocketServer.sendMessageOne(k, dataPackages);
        });
    }
}
