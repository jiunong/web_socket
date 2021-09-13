package com.xcloud.svg.index;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.xcloud.svg.socket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.File;

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

    @GetMapping("json/{fileName}")
    public String jsonView(@PathVariable String fileName,HttpServletRequest request){

        File file0 = FileUtil.file("C:\\svg\\".concat(fileName).concat("0.json"));
        File file1 = FileUtil.file("C:\\svg\\".concat(fileName).concat("1.json"));
        File file2 = FileUtil.file("C:\\svg\\".concat(fileName).concat("2.json"));
        File file3 = FileUtil.file("C:\\svg\\".concat(fileName).concat("3.json"));
        File file4 = FileUtil.file("C:\\svg\\".concat(fileName).concat("4.json"));
        //JSONObject jsonObject = JSONUtil.parseObj(FileUtil.readString(file, CharsetUtil.UTF_8));
        JSONArray list0 =  (JSONArray)JSONUtil.parseObj(FileUtil.readString(file0, CharsetUtil.UTF_8)).get("content");
        JSONArray list1 =  (JSONArray)JSONUtil.parseObj(FileUtil.readString(file1, CharsetUtil.UTF_8)).get("content");
        JSONArray list2 =  (JSONArray)JSONUtil.parseObj(FileUtil.readString(file2, CharsetUtil.UTF_8)).get("content");
        JSONArray list3 =  (JSONArray)JSONUtil.parseObj(FileUtil.readString(file3, CharsetUtil.UTF_8)).get("content");
        JSONArray list4 =  (JSONArray)JSONUtil.parseObj(FileUtil.readString(file4, CharsetUtil.UTF_8)).get("content");

        list0.addAll(list1);
        list0.addAll(list2);
        list0.addAll(list3);
        list0.addAll(list4);

  
        request.setAttribute("list",list0);
        return "json";
    }

}
