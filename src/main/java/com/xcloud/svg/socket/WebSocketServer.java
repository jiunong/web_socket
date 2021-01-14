package com.xcloud.svg.socket;

import cn.hutool.core.date.DateTime;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.xcloud.svg.socket.SocketAddress.getRemoteAddress;

/**
 * TODO
 *
 * @author xuhong.ding
 * @since 2021/1/4 14:50
 */
@Slf4j
@Component
@ServerEndpoint(value = "/websocket")
public class WebSocketServer {
    // 静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onlineCount = 0;

    // concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。若要实现服务端与单一客户端通信的话，可以使用Map来存放，其中Key可以为用户标识
    private static CopyOnWriteArraySet<WebSocketServer> webSocketSet = new CopyOnWriteArraySet<WebSocketServer>();

    private static Vector<Session> clients = new Vector<>();

    private static HashMap<Session, List<DataPackage>> msgStream = new HashMap<>();

    // 与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;
    private String ipaddr;

    /**
     * 连接建立成功调用的方法
     *
     * @param session 可选的参数。session为与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    @OnOpen
    public void onOpen(Session session) {
        clients.add(session);
        this.session = session;
        webSocketSet.add(this); // 加入set中
        addOnlineCount(); // 在线数加1
        System.out.println(DateTime.now() + ":有新连接加入！当前在线人数为" + getOnlineCount());
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        clients.remove(session);
        webSocketSet.remove(this); // 从set中删除
        subOnlineCount(); // 在线数减1
        System.out.println(DateTime.now() + ":有一连接关闭！当前在线人数为" + getOnlineCount());
    }

    /**
     * 收到客户端消息确认是否被消费
     *
     * @param message 客户端发送过来的消息
     * @param session 可选的参数
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        RemoteEndpoint.Async asyncRemote = session.getAsyncRemote();
        InetSocketAddress remoteAddress = getRemoteAddress(session);
        String msg = "来自客户端" + remoteAddress + "的消息:" + message;
        System.out.println(msg);
        // 群发消息
        for (WebSocketServer item : webSocketSet) {
            try {
                item.sendMessage(msg);
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
        }
    }

    /**
     * 发生错误时调用
     *
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        System.out.println("发生错误");
        error.printStackTrace();
    }

    /**
     * 这个方法与上面几个方法不一样。没有用注解，是根据自己需要添加的方法。
     *
     * @param message
     * @throws IOException
     */
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
        // this.session.getAsyncRemote().sendText(message);
    }

    /**
     * TODO 给所有连接的客户端发送消息
     *
     * @param message
     * @return void
     * @author xuhong.ding
     * @since 2021/1/14 9:18
     */
    public static void sendMessageAll(String message) {
        clients.forEach(u -> {
            List<DataPackage> dataPackages = Optional.ofNullable( msgStream.get(u)).orElse(new LinkedList<>());
            DataPackage dataPackage = DataPackage.builder()
                    .id(UUID.randomUUID().toString())
                    .data(message)
                    .status(MsgStatus.ALIVE)
                    .onTime(DateTime.now().toString())
                    .retryTime(0)
                    .build();
            dataPackages.add(dataPackage);
            msgStream.put(u, dataPackages);
            u.getAsyncRemote().sendText(JSONUtil.toJsonStr(dataPackage));
        });
    }

    /**
    * TODO
    * @param session 客户端
    * @param List<DataPackage> 数据包集合
    * @return void
    * @author xuhong.ding
    * @since 2021/1/14 14:52
    */
    public static void sendMessageOne(Session session,List<DataPackage> dataPackages){
        msgStream.put(session, dataPackages);
        dataPackages.forEach(dataPackage -> {
            session.getAsyncRemote().sendText(JSONUtil.toJsonStr(dataPackage));
            log.info("向客户端{}重发消息{}",SocketAddress.getRemoteAddress(session),JSONUtil.toJsonStr(dataPackage));
        });
    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        WebSocketServer.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        WebSocketServer.onlineCount--;
    }

    public static HashMap<Session, List<DataPackage>> getMsgStream() {
        return msgStream;
    }
}
