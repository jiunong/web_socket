package com.xcloud.svg.index;

import cn.hutool.core.date.DateTime;
import com.xcloud.svg.socket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * TODO
 *
 * @author xuhong.ding
 * @since 2020/11/19 15:35
 */
@Controller
@Slf4j
public class IndexController {


    @RequestMapping("i/{src}")
    public String svg(@PathVariable String src,HttpServletRequest request) {
        request.setAttribute("src",src);
        System.out.println(src);
        return "svg";
    }

    @RequestMapping("socket")
    public String webSocket(){
        return "socket";
    }


    @GetMapping("sendMessage/{message}")
    @ResponseBody
    public String sendMessage(@PathVariable String message ){
        String s = message + DateTime.now();
        WebSocketServer.sendMessageAll(s);
        log.info("向所有客户端发送了消息{}",s);
        return message;
    }

}
