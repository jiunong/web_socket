package com.xcloud.svg.index;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSONObject;
import com.xcloud.svg.kafka.producer.KafkaSender;
import com.xcloud.svg.pojo.MyBatisLog;
import com.xcloud.svg.pojo.PoleTransformer;
import com.xcloud.svg.pojo.PsrType;
import com.xcloud.svg.service.svg.SvgService;
import com.xcloud.svg.service.svg.XmlServlce;
import com.xcloud.svg.socket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.DocumentException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO
 *
 * @author xuhong.ding
 * @since 2020/11/19 15:35
 */
@Controller
@Slf4j
public class IndexController {

    private static String PATH = "C:\\svg\\";

    @Resource
    private KafkaSender kafkaSender;

    @RequestMapping("i/{src}")
    public String svg(@PathVariable String src, HttpServletRequest request) {
        request.setAttribute("src", src);
        System.out.println(src);
        return "svg";
    }

    @RequestMapping("socket")
    public String webSocket() {
        return "socket";
    }

    @RequestMapping("tree")
    public String svgTree() {
        return "svgTree";
    }


    @RequestMapping("node")
    @ResponseBody
    public List<String> node(String rdfIds) throws Exception {
        List<String> list = ListUtil.toList(rdfIds.split(","));
        return SvgService.getTdfw("211公皋线单线图.sln.xml", list);
    }

    @GetMapping("sendMessage/{message}")
    @ResponseBody
    public String sendMessage(@PathVariable String message) {
        String s = message + DateTime.now();
        WebSocketServer.sendMessageAll(s);
        log.info("向所有客户端发送了消息{}", s);
        return message;
    }

    @GetMapping("json/{fileName}")
    public String jsonView(@PathVariable String fileName, HttpServletRequest request) {

        File file0 = FileUtil.file("C:\\svg\\".concat(fileName).concat("0.json"));
        File file1 = FileUtil.file("C:\\svg\\".concat(fileName).concat("1.json"));
        /*
        File file2 = FileUtil.file("C:\\svg\\".concat(fileName).concat("2.json"));
        File file3 = FileUtil.file("C:\\svg\\".concat(fileName).concat("3.json"));
        File file4 = FileUtil.file("C:\\svg\\".concat(fileName).concat("4.json"));*/
        //JSONObject jsonObject = JSONUtil.parseObj(FileUtil.readString(file, CharsetUtil.UTF_8));
        JSONArray list0 = (JSONArray) JSONUtil.parseObj(FileUtil.readString(file0, CharsetUtil.UTF_8)).get("content");
        JSONArray list1 = (JSONArray) JSONUtil.parseObj(FileUtil.readString(file1, CharsetUtil.UTF_8)).get("content");
        /*
        JSONArray list2 = (JSONArray) JSONUtil.parseObj(FileUtil.readString(file2, CharsetUtil.UTF_8)).get("content");
        JSONArray list3 = (JSONArray) JSONUtil.parseObj(FileUtil.readString(file3, CharsetUtil.UTF_8)).get("content");
        JSONArray list4 = (JSONArray) JSONUtil.parseObj(FileUtil.readString(file4, CharsetUtil.UTF_8)).get("content");

        list0.addAll(list2);
        list0.addAll(list3);
        list0.addAll(list4);*/
        list0.addAll(list1);

        request.setAttribute("list", list0);
        return "json";
    }


    /**
     * TODO 详图
     *
     * @param fileName
     * @return
     * @throws Exception
     */
    @RequestMapping("treeData/{fileName}")
    @ResponseBody
    public JSONObject treeData(@PathVariable String fileName) throws Exception {
        return SvgService.findAll("C:\\svg\\" + fileName);
    }

    /**
     * TODO 简图
     *
     * @param fileName
     * @return
     * @throws Exception
     */
    @RequestMapping("treeData/simple/{fileName}")
    @ResponseBody
    public JSONObject treeDataSimple(@PathVariable String fileName) throws Exception {
        return SvgService.findAllSimple("C:\\svg\\" + fileName);
    }

    /**
     * TODO 数据图
     *
     * @param fileName
     * @return
     * @throws Exception
     */
    @RequestMapping("treeData/tree/{fileName}")
    @ResponseBody
    public JSONObject treeDataTree(@PathVariable String fileName) throws Exception {
        return SvgService.findAllData("C:\\svg\\" + fileName);
    }

    @Deprecated
    @RequestMapping("treeData1")
    @ResponseBody
    public JSONObject treeData() throws Exception {
        return SvgService.findAll(ListUtil.of(PATH + "shenyang_沈阳运检部_农大变80970农毛甲线单线图.sln.xml", PATH + "shenyang_沈阳运检部_农大变80972农毛乙线单线图.sln.xml"));
    }

    @Deprecated
    @RequestMapping("treeData2")
    @ResponseBody
    public JSONObject treeData1() throws Exception {
        return SvgService.findAll(PATH + "518园区线单线图.sln.xml", "PD_30500000_271285");
    }

    @Deprecated
    @RequestMapping("treeData12")
    @ResponseBody
    public JSONObject treeData4() throws Exception {
        return SvgService.findAll(PATH + "211公皋线单线图.sln.xml", "PD_30500000_276163", "C:\\svg\\518园区线单线图.sln.xml");
    }

    @Deprecated
    @RequestMapping("treeData3")
    @ResponseBody
    public JSONObject treeData2() throws Exception {
        return SvgService.findAll("C:\\svg\\113疏豪线单线图.sln.xml", "PD_31100000_288122");
    }

    @Deprecated
    @RequestMapping("treeData4")
    @ResponseBody
    public JSONObject treeData3() throws Exception {
        return SvgService.findAll("C:\\svg\\111现代线单线图.sln.xml", "PD_30500000_305465");
    }

    @Deprecated
    @RequestMapping("treeData34")
    @ResponseBody
    public JSONObject treeData34() throws Exception {
        return SvgService.findAll("C:\\svg\\113疏豪线单线图.sln.xml", "PD_31100000_288122", "C:\\svg\\111现代线单线图.sln.xml");
    }

    @Deprecated
    @RequestMapping("treeData5")
    @ResponseBody
    public JSONObject treeData5() throws Exception {
        return SvgService.findAll("C:\\svg\\103东联线单线图.sln.xml", "PD_30500000_274491");
    }

    @GetMapping("logs")
    public String mybatislog(HttpServletRequest request) {
        List<MyBatisLog> logs = ListUtil.list(false);
        List<File> mybatisLogs = FileUtil.loopFiles("C:\\logs\\", u -> u.getName().contains("mybatis_log"));
        mybatisLogs.forEach(u -> {
            List<String> strings = FileUtil.readUtf8Lines(u);
            strings.forEach(v -> {
                if (StrUtil.isNotEmpty(v)) {
                    MyBatisLog myBatisLog = new MyBatisLog(v);
                    logs.add(myBatisLog);
                }
            });
        });
        request.setAttribute("logs", logs);
        return "logs";
    }


    @RequestMapping("poles/{fileName}")
    public void poles(HttpServletResponse response, @PathVariable String fileName) throws Exception {
        fileName = StrUtil.isEmpty(fileName) ? "211公皋线单线图.sln.xml" : fileName;
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        //如果EXPORT_PATH不存在，则创建
        String filename = DateUtil.current() + ".xlsx";
        String encode = URLEncoder.encode(filename, "UTF-8");
        response.setHeader("Content-disposition", "attachment;filename=" + encode);
        String finalFileName = fileName;
        EasyExcel.write(response.getOutputStream(), PoleTransformer.class)
                .sheet("211公皋线单线图").doWrite(() -> {
                    try {
                        return XmlServlce.getPoles("C:\\svg\\" + finalFileName);
                    } catch (DocumentException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @RequestMapping("psrTypes")
    public void poles(HttpServletResponse response) throws Exception {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        //如果EXPORT_PATH不存在，则创建
        String filename = "psrtype" + DateUtil.current() + ".xlsx";
        String encode = URLEncoder.encode(filename, "UTF-8");
        response.setHeader("Content-disposition", "attachment;filename=" + encode);
        EasyExcel.write(response.getOutputStream(), PsrType.class)
                .sheet("211公皋线单线图").doWrite(() -> {
                    try {
                        return XmlServlce.getPsrType(null);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @GetMapping("/sendMessageToKafka")
    public String sendMessageToKafka() {
        Map<String, String> messageMap = new HashMap();
        messageMap.put("message", "我是一条消息");
        String taskid = "123456";
        String jsonStr = JSONObject.toJSONString(messageMap);
        //kakfa的推送消息方法有多种，可以采取带有任务key的，也可以采取不带有的（不带时默认为null）
        System.out.println("发送了消息：" + jsonStr);
        kafkaSender.send("ZT_DMS_JSSOMS", taskid, jsonStr);
        return "hi guy!";
    }

    @ResponseBody
    @PostMapping("/sendMessageToKafka2")
    public String sendMessageToKafka2() {
        Map<String, String> messageMap = new HashMap();
        messageMap.put("message", "111我是一条消息");
        String taskid = "123456";
        String jsonStr = JSONObject.toJSONString(messageMap);
        //kakfa的推送消息方法有多种，可以采取带有任务key的，也可以采取不带有的（不带时默认为null）
        System.out.println("发送了消息：" + jsonStr);
        kafkaSender.send("ZT_DMS_JSSOMS", taskid, jsonStr);
        return "hi guy!";
    }
}
