package com.xcloud.svg.service.svg;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.xcloud.svg.util.LogsHandler;
import com.xcloud.svg.util.XmlUtil;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SvgService {
    /*图源标签属性*/
    public static final String RDF_ID = "ID";
    public static final String MRID = "IdentifiedObject.mRID";
    public static final String NAME = "IdentifiedObject.name";
    public static final String SWITCH_OPEN = "Switch.open";
    public static final String SWITCH_NORMAL_OPEN = "Switch.normalOpen";
    public static final String VECTOR_GROUP = "PowerTransformer.vectorGroup";
    public static final String EQUIPMENT_CONTAINER = "Equipment.EquipmentContainer";
    public static final String CONDUCTING_EQUIPMENT = "Terminal.ConductingEquipment";
    //feeder NORMAL_ENERGIZING_SUBSTATION = breaker CONTAINER
    public static final String NORMAL_ENERGIZING_SUBSTATION = "Feeder.NormalEnergizingSubstation";
    public static final String CONTAINER = "Equipment.EquipmentContainer";
    public static final String FEEDER_ISCURRENTFEEDER = "Feeder.IsCurrentFeeder";

    public static final String CONNECTIVITY_NODE = "Terminal.ConnectivityNode";
    public static final String PSRTYPE = "PowerSystemResource.PSRType";
    public static final String POLECODE = "PoleCode";
    public static final String TERMINAL = "Terminal";
    public static final String POLECODETERMINAL = "PoleCode.Terminal";
    public static final String FEEDER = "Feeder";
    //开关标签
    public static final String BREAKER = "Breaker";
    public static final String DISCONNECTOR = "Disconnector";
    public static final String LOADBREAKSWITCH = "LoadBreakSwitch";
    public static final String GROUNDDISCONNECTOR = "GroundDisconnector";
    public static final String POWERTRANSFORMER = "PowerTransformer";
    public static final String CIM_PSRTYPE = "PSRType";
    //变压器标签
    public static final String PowerTransformer = "PowerTransformer";

    private static final String RELATIVEPATH = "C:\\svg\\";

    private static List<Node> nodes = ListUtil.list(false);
    private static Node firstNode = Node.builder().build();

    public static void main(String[] args) throws Exception {
        Node pd_30500000_276163 = findAllNode("211公皋线单线图.sln.xml");
        System.out.println("加载之前" + nodes.size());
        findNode(pd_30500000_276163, "PD_11300000_197339");
        System.out.println("加载之后" + nodes.size());

    }

    public static List<String> getTdfw(String fileName, List<String> rdfId) throws Exception {
        nodes.clear();
        firstNode = Node.builder().build();
        String fisrt = rdfId.remove(0);
        Node allNode = findAllNode(fileName);
        findNode(allNode, fisrt);//初始化第一个节点
        System.out.println("加载之前" + nodes.size());
        findNode(firstNode, rdfId);//初始化其他节点
        System.out.println("加载之后" + nodes.size());
        return nodes.stream().map(u -> u.getValue()).collect(Collectors.toList());
    }


    public static JSONObject findAll(String fileName) throws Exception {
        List<JSONObject> list = ListUtil.list(false);

        JSONObject jsonObject = XmlUtil.xmlJsonObj(FileUtil.readString(RELATIVEPATH + fileName, StandardCharsets.UTF_8));
        jsonObject.keySet().forEach(u -> {
            List<JSONObject> data = ObjectToJsonList(jsonObject.get(u));
            list.addAll(data);
        });
        //获取输入线路开关
        List<JSONObject> switches = findSwitches(jsonObject);
        //获取输入线路关联线路的所有设备清单
        List<JSONObject> aFalse = ObjectToJsonList(jsonObject.get(FEEDER)).stream().filter(u -> u.get(FEEDER_ISCURRENTFEEDER).equals("false")).collect(Collectors.toList());
        aFalse.stream().forEach(u -> {
            //通过rdfId去feeder表查询图形文件名称
            String s = findFileName(u.get(RDF_ID).toString());
            try {
                JSONObject jsonObject1 = XmlUtil.xmlJsonObj(FileUtil.readString(RELATIVEPATH + s, StandardCharsets.UTF_8));
                List<JSONObject> bak = findSwitches(jsonObject1);
                switches.addAll(bak);
                jsonObject1.keySet().forEach(v -> {
                    List<JSONObject> data = ObjectToJsonList(jsonObject.get(v));
                    list.addAll(data);
                });
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        });

        //找到线路的出口开关
        JSONObject aTrue = ObjectToJsonList(jsonObject.get(FEEDER)).stream().filter(u -> u.get(FEEDER_ISCURRENTFEEDER).equals("true")).findFirst().get();
        String connect = aTrue.get(NORMAL_ENERGIZING_SUBSTATION).toString();
        JSONObject firstBreaker = ObjectToJsonList(jsonObject.get(BREAKER)).stream().filter(u -> u.get(CONTAINER).equals(connect)).findFirst().get();
        String id = Optional.ofNullable(firstBreaker).map(u -> u.get(RDF_ID).toString().replace("#", "")).get();


        Node p = Node.builder().value("root").name("根").build();
        beginLoop(list, p, id);
        String s = JSONObject.toJSONString(p);
        return JSONObject.parseObject(s);

    }

    public static JSONObject findAll(String relativePath, String id) throws Exception {
        JSONObject jsonObject = XmlUtil.xmlJsonObj(FileUtil.readString(relativePath, StandardCharsets.UTF_8));
        List<JSONObject> list = ListUtil.list(false);
        jsonObject.keySet().forEach(u -> {
            List<JSONObject> data = ObjectToJsonList(jsonObject.get(u));
            list.addAll(data);
        });
        Node p = Node.builder().value("root").name("根").build();
        beginLoop(list, p, id);
        String s = JSONObject.toJSONString(p);
        return JSONObject.parseObject(s);

    }

    public static JSONObject findAll(String relativePath, String id, String path2) throws Exception {
        findSame(relativePath, path2);
        JSONObject jsonObject = XmlUtil.xmlJsonObj(FileUtil.readString(relativePath, StandardCharsets.UTF_8));
        JSONObject jsonObject2 = XmlUtil.xmlJsonObj(FileUtil.readString(path2, StandardCharsets.UTF_8));
        List<JSONObject> list = ListUtil.list(false);
        jsonObject.keySet().forEach(u -> {
            List<JSONObject> data = ObjectToJsonList(jsonObject.get(u));
            list.addAll(data);
        });
        jsonObject2.keySet().forEach(u -> {
            List<JSONObject> data = ObjectToJsonList(jsonObject2.get(u));
            list.addAll(data);
        });
        List<JSONObject> result = list.stream().distinct().collect(Collectors.toList());
        Node p = Node.builder().value("root").name("根").build();
        beginLoop(result, p, id);
        String s = JSONObject.toJSONString(p);
        return JSONObject.parseObject(s);
    }

    public static Node findAllNode(String fileName) throws Exception {
        List<JSONObject> list = ListUtil.list(false);

        JSONObject jsonObject = XmlUtil.xmlJsonObj(FileUtil.readString(RELATIVEPATH + fileName, StandardCharsets.UTF_8));
        jsonObject.keySet().forEach(u -> {
            List<JSONObject> data = ObjectToJsonList(jsonObject.get(u));
            list.addAll(data);
        });
        //获取输入线路开关
        List<JSONObject> switches = findSwitches(jsonObject);
        //获取输入线路关联线路的所有设备清单
        List<JSONObject> aFalse = ObjectToJsonList(jsonObject.get(FEEDER)).stream().filter(u -> u.get(FEEDER_ISCURRENTFEEDER).equals("false")).collect(Collectors.toList());
        aFalse.stream().forEach(u -> {
            //通过rdfId去feeder表查询图形文件名称
            String s = findFileName(u.get(RDF_ID).toString());
            try {
                JSONObject jsonObject1 = XmlUtil.xmlJsonObj(FileUtil.readString(RELATIVEPATH + s, StandardCharsets.UTF_8));
                List<JSONObject> bak = findSwitches(jsonObject1);
                switches.addAll(bak);
                jsonObject1.keySet().forEach(v -> {
                    List<JSONObject> data = ObjectToJsonList(jsonObject.get(v));
                    list.addAll(data);
                });
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        });

        //找到线路的出口开关
        JSONObject aTrue = ObjectToJsonList(jsonObject.get(FEEDER)).stream().filter(u -> u.get(FEEDER_ISCURRENTFEEDER).equals("true")).findFirst().get();
        String connect = aTrue.get(NORMAL_ENERGIZING_SUBSTATION).toString();
        JSONObject firstBreaker = ObjectToJsonList(jsonObject.get(BREAKER)).stream().filter(u -> u.get(CONTAINER).equals(connect)).findFirst().get();
        String id = Optional.ofNullable(firstBreaker).map(u -> u.get(RDF_ID).toString().replace("#", "")).get();


        Node p = Node.builder().value("root").name("根").build();
        beginLoop(list, p, id);
        return p;
    }

    public static JSONObject findSame(String relativePath, String relativePath2) throws Exception {
        JSONObject jsonObject = XmlUtil.xmlJsonObj(FileUtil.readString(relativePath, StandardCharsets.UTF_8));
        JSONObject jsonObject2 = XmlUtil.xmlJsonObj(FileUtil.readString(relativePath2, StandardCharsets.UTF_8));
        List<JSONObject> list = ListUtil.list(false);
        List<JSONObject> list2 = ListUtil.list(false);
        jsonObject.keySet().forEach(u -> {
            List<JSONObject> data = ObjectToJsonList(jsonObject.get(u));
            list.addAll(data);
        });
        jsonObject2.keySet().forEach(u -> {
            List<JSONObject> data = ObjectToJsonList(jsonObject2.get(u));
            list2.addAll(data);
        });
        System.out.println("相同数据开始展示###########################");
        List<String> collect2 = list2.stream().map(u -> u.getString(MRID)).collect(Collectors.toList());
        list.forEach(u -> {
            if (collect2.contains(u.getString(MRID))) {
                System.out.println(u.toJSONString());
            }
        });
        System.out.println("相同数据结束展示###########################");
        return JSONObject.parseObject("");
        /* System.out.println("处理后数据长度为"+list.size());*/

    }


    /**
     * TODO 开始对所有设备拉手
     *
     * @param list :
     * @param p    :
     * @param id   : 出口开关
     * @return void
     * @author xuhong.ding
     * @since 2022/4/21 14:54
     **/
    public static void beginLoop(List<JSONObject> list, Node p, String id) {

        JSONObject o1 = first_findAndDelete(list, id);

        Node x = Node.builder().value(o1.getString(RDF_ID)).name(o1.getString(NAME)).build();
        List<JSONObject> o2 = second_findTerminals(list, id);
        o2.forEach(u -> {
            if (StrUtil.isNotEmpty(u.getString(CONNECTIVITY_NODE))) {
                Node t = Node.builder().value(u.getString(RDF_ID)).name("TERMINAL").build();
                JSONObject o3 = third_findConnectivityNodes(list, u.getString(CONNECTIVITY_NODE));
                if (StrUtil.isNotEmpty(o3.getString(RDF_ID))) {
                    Node c = Node.builder().value(o3.getString(RDF_ID)).name("CONNECTIVITY_NODE").build();
                    List<JSONObject> o4 = forth_findTerminalsByConnectivityNode(list, o3.getString(RDF_ID));
                    o4.forEach(v -> {
                        beginLoop(list, c, v.getString(CONDUCTING_EQUIPMENT).replace("#", ""));
                    });
                    t.appendChild(c);
                }
                x.appendChild(t);
            }
        });
        p.appendChild(x);

    }


    public static List<JSONObject> ObjectToJsonList(Object o) {
        List<JSONObject> list = ListUtil.list(false);
        if (o == null) {

        } else if (o instanceof JSONObject) {
            list.add((JSONObject) o);
        } else if (o instanceof String) {
            list.add(JSONObject.parseObject(o.toString()));
        } else {
            list.addAll((List<JSONObject>) o);
        }
        return list;
    }

    //1 查找指定的导电设备
    public static JSONObject first_findAndDelete(List<JSONObject> list, String id) {
        JSONObject o = new JSONObject();
        Iterator<JSONObject> iterator = list.iterator();
        while (iterator.hasNext()) {
            JSONObject u = iterator.next();
            if (u.get(RDF_ID).equals(id)) {
                iterator.remove();
                o = u;
                LogsHandler.instance().logs("1找到并删除导电设备{}", u.toString());
            }
        }
        return o;
    }

    //2 通过导电设备的id 找到链接的端子
    public static List<JSONObject> second_findTerminals(List<JSONObject> list, String id) {

        List<JSONObject> l = ListUtil.list(false);
        Iterator<JSONObject> iterator = list.iterator();
        while (iterator.hasNext()) {
            JSONObject u = iterator.next();
            if (u.get(RDF_ID).toString().contains(id)) {
                l.add(u);
                iterator.remove();
                LogsHandler.instance().logs("通过{}找到并删除端子{}", id, u.toString());
            }
        }
        return l;
    }

    //3 通过端子的connectivityNode 找到连接点
    public static JSONObject third_findConnectivityNodes(List<JSONObject> list, String connectivityNode) {
        JSONObject o = new JSONObject();
        Iterator<JSONObject> iterator = list.iterator();
        while (iterator.hasNext()) {
            JSONObject u = iterator.next();
            if (("#".concat(u.getString(RDF_ID))).equals(connectivityNode)) {
                iterator.remove();
                o = u;
                LogsHandler.instance().logs("1找到并删除连接点{}", u.toString());
            }
        }
        return o;
    }

    //4通过连接点的id 找到所有关联的端子
    public static List<JSONObject> forth_findTerminalsByConnectivityNode(List<JSONObject> list, String connectivityNode) {
        List<JSONObject> l = ListUtil.list(false);
        Iterator<JSONObject> iterator = list.iterator();
        while (iterator.hasNext()) {
            JSONObject u = iterator.next();
            if (StrUtil.isNotEmpty(u.getString(CONNECTIVITY_NODE)) && u.getString(CONNECTIVITY_NODE).contains(connectivityNode)) {
                l.add(u);
                iterator.remove();
                LogsHandler.instance().logs("通过{}找到并删除端子{}", connectivityNode, u.toString());
            }
        }
        return l;
    }
    //5 通过端子的 ConductingEquipment 寻找指定的导电设备 回到1


    //findFileName
    public static String findFileName(String rdfId) {
        String[] split = rdfId.split("/");
        return "";
    }


    //挑出jsonObject中的开关
    public static List<JSONObject> findSwitches(JSONObject object) {
        List<JSONObject> data = ListUtil.list(false);
        List<JSONObject> b = ObjectToJsonList(object.get(BREAKER));
        List<JSONObject> d = ObjectToJsonList(object.get(DISCONNECTOR));
        List<JSONObject> g = ObjectToJsonList(object.get(GROUNDDISCONNECTOR));
        List<JSONObject> l = ObjectToJsonList(object.get(LOADBREAKSWITCH));
        data.addAll(b);
        data.addAll(d);
        data.addAll(g);
        data.addAll(l);
        return data;
    }

    /**
     * TODO 填充nodes
     *
     * @param node    :
     * @param brakers :  list集合
     * @return void
     * @author xuhong.ding
     * @since 2022/4/21 14:52
     **/
    public static void findNode(Node node, List<String> brakers) {
        if (node == null || brakers.contains(node.getValue())) {
            return;
        }
        if (node.getChildren() == null) {
            Node rs = Node.builder().name(node.getName()).value(node.getValue()).build();
            nodes.add(rs);
            return;
        }
        Node rs = Node.builder().name(node.getName()).value(node.getValue()).build();
        nodes.add(rs);
        node.getChildren().forEach(u -> {
            findNode(u, brakers);
        });

    }

    /**
     * TODO 根据节点名称获取节点
     *
     * @param node   : 节点
     * @param braker : 开关id
     * @return void
     * @author xuhong.ding
     * @since 2022/4/21 14:24
     **/
    public static void findNode(Node node, String braker) {
        if (node == null || node.getChildren() == null) {
            return;
        }
        if (node.getValue().equals(braker)) {
            firstNode = node;
            return;
        }
        node.getChildren().forEach(u -> {
            findNode(u, braker);
        });
    }

}
