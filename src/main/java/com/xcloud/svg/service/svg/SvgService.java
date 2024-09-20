package com.xcloud.svg.service.svg;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.xcloud.svg.pojo.SvgNode;
import com.xcloud.svg.pojo.SvgLinkModel;
import com.xcloud.svg.util.LogsHandler;
import com.xcloud.svg.util.XmlUtil;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class SvgService {
    /*图源标签属性*/
    public static final String RDF_ID = "ID";
    public static final String PSR_ID = "IdentifiedObject.PowerSystemResourceID";
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
    public static final String SUB_STATION = "Substation";
    public static final String DISCONNECTOR = "Disconnector";
    public static final String LOADBREAKSWITCH = "LoadBreakSwitch";
    public static final String GROUNDDISCONNECTOR = "GroundDisconnector";
    public static final String POWERTRANSFORMER = "PowerTransformer";
    public static final String CIM_SUBSTATION = "Substation";
    public static final String CIM_PSRTYPE = "PSRType";
    public static final String CIM_VOLTAGELEVEL = "VoltageLevel";
    public static final String BUSBARSECTION = "BusbarSection";
    public static final String TYPE = "TYPE";
    public static final String CONTAINER_ID = "CONTAINER_ID";
    public static final String CONTAINER_NAME = "CONTAINER_NAME";
    public static final String CONTAINER_TYPE = "CONTAINER_TYPE";
    //变压器标签
    public static final String PowerTransformer = "PowerTransformer";

    private static final String RELATIVEPATH = "C:\\svg\\";

    private static List<SvgNode> nodes = ListUtil.list(false);
    private static SvgNode firstNode = SvgNode.builder().build();
    static List<SvgNode> allNode = ListUtil.list(false);
    static List<SvgLinkModel> svgLinkModelList = ListUtil.list(false);

    public static void main(String[] args) throws Exception {
        SvgNode pd_30500000_276163 = findAllNode("211公皋线单线图.sln.xml");
        System.out.println("加载之前" + nodes.size());
        findNode(pd_30500000_276163, "PD_11300000_197339");
        System.out.println("加载之后" + nodes.size());

    }

    public static List<String> getTdfw(String fileName, List<String> rdfId) throws Exception {
        nodes.clear();
        firstNode = SvgNode.builder().build();
        String fisrt = rdfId.remove(0);
        SvgNode allNode = findAllNode(fileName);
        findNode(allNode, fisrt);//初始化第一个节点
        System.out.println("加载之前" + nodes.size());
        findNode(firstNode, rdfId);//初始化其他节点
        System.out.println("加载之后" + nodes.size());
        return nodes.stream().map(u -> u.getValue()).collect(Collectors.toList());
    }


    /**
     * TODO 单线详图
     *
     * @param fileName 单一文件
     * @return 树结构
     */
    public static JSONObject findAll(String fileName) throws Exception {

        if (fileName.contains("node.xml")) {
            return JSONObject.parseObject(FileUtil.readString(fileName, StandardCharsets.UTF_8));
        }
        List<JSONObject> list = ListUtil.list(false);

        JSONObject jsonObject = XmlUtil.xmlJsonObj(FileUtil.readString(fileName, StandardCharsets.UTF_8));
        for (String u : jsonObject.keySet()) {
            List<JSONObject> data = ObjectToJsonList(jsonObject.get(u));
            data.forEach(d -> d.put(TYPE, u));
            list.addAll(data);
        }
        //获取输入线路开关
        List<JSONObject> switches = findSwitches(jsonObject);
        //获取输入线路关联线路的所有设备清单
        List<JSONObject> linkFeeder = ObjectToJsonList(jsonObject.get(FEEDER)).stream().filter(u -> u.get(FEEDER_ISCURRENTFEEDER).equals("false")).collect(Collectors.toList());
        /*   for (JSONObject u : linkFeeder) {
            //通过rdfId去feeder表查询图形文件名称
            String s = findFileName(u.get(RDF_ID).toString());
            try {
                JSONObject jsonObject1 = XmlUtil.xmlJsonObj(FileUtil.readString(RELATIVEPATH + s, StandardCharsets.UTF_8));
                List<JSONObject> bak = findSwitches(jsonObject1);
                switches.addAll(bak);
                for (String u2 : jsonObject1.keySet()) {
                    List<JSONObject> data = ObjectToJsonList(jsonObject1.get(u2));
                    data.forEach(d -> d.put(TYPE, u2));
                    list.addAll(data);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }*/
        /*清空重复数据 方便拉手*/
        list = list.stream().distinct().collect(Collectors.toList());
        //找到线路的出口开关 aTrue:当前线路 connect:当前线路
        JSONObject currentFeeder = ObjectToJsonList(jsonObject.get(FEEDER)).stream().filter(u -> u.get(FEEDER_ISCURRENTFEEDER).equals("true")).findFirst().get();
        String station66kvId = currentFeeder.getString(NORMAL_ENERGIZING_SUBSTATION);

        JSONObject station66kv = StrUtil.isNotEmpty(station66kvId) ? ObjectToJsonList(jsonObject.get(CIM_SUBSTATION)).stream().filter(u -> station66kvId.contains(u.getString(RDF_ID))).collect(Collectors.toList()).stream().findFirst().get() :
                ObjectToJsonList(jsonObject.get(SUB_STATION)).stream().filter(u->u.getString(PSRTYPE).equals("#PD_30000000")).findFirst().orElse(null);
        JSONObject firstBreaker = ObjectToJsonList(jsonObject.get(BREAKER)).stream().filter(u -> u.getString(CONTAINER).contains( station66kv.getString(RDF_ID))).findFirst().get();
        String id = Optional.ofNullable(firstBreaker).map(u -> u.get(RDF_ID).toString().replace("#", "")).get();

        SvgNode p = SvgNode.builder().value(station66kv.getString(RDF_ID)).id(station66kv.getString(RDF_ID)).psrId(station66kv.getString(PSR_ID)).name(station66kv.getString(NAME)).type("Substation").build();
        beginLoop(list, p, id);


        SvgNode busbar = p.getChildren().get(0).getChildren().stream().filter(u -> BUSBARSECTION.equals(u.getType())).findFirst().orElse(SvgNode.busbarSection());
        p.getChildren().get(0).removeChild(busbar);
        List<SvgNode> children = p.getChildren();
        busbar.setChildren(ListUtil.list(false));
        busbar.appendChildren(children);
        busbar.setParentId(p.getId());
        busbar.setContainerId(p.getId());
        busbar.setContainerName(p.getName());
        busbar.setContainerType(p.getType());
        p.setChildren(ListUtil.list(false));
        p.appendChild(busbar);

        String s = JSONObject.toJSONString(p);
        return JSONObject.parseObject(s);

    }


    /**
     * TODO 单线 {{简图}}  保存连接关系link.xml
     *
     * @param fileName
     * @return
     */
    public static JSONObject findAllSimple(String fileName) throws Exception {
        JSONObject all = findAll(fileName);
        SvgNode svgNode = JSONObject.parseObject(JSONObject.toJSONString(all), SvgNode.class);
        postVisit(svgNode, null);

        List<String> tttags = allNode.stream().filter(u -> ListUtil.of("绿纳").contains(u.getContainerName())).map(SvgNode::getValue).collect(Collectors.toList());
        tags = ListUtil.list(false);
        tags.addAll(tttags);
        //tags.add("b4fef3c9-0eae-485f-bfe8-9d54b377a419");
        //tags.add("4fd46025-9b1e-4756-82e7-8de7a651a7c3");
        //tags.add("12376c22-54d5-48b8-9892-f14f21c98caf");
        //tags.add("0781d340-4821-4d1a-8ccc-1845832c7ec9");
        //tags.add("PD_10200000_5492385");
        preVisit(svgNode, null);
        inorderVisitBusBar(svgNode, null);
        inorderVisitClearACLine(svgNode, null);
        initChildren(svgNode);
        getLinkNode(svgNode);
        FileUtil.writeString(JSONObject.toJSONString(svgLinkModelList, SerializerFeature.DisableCircularReferenceDetect), "C:\\svg\\link.xml", StandardCharsets.UTF_8);
        return JSONObject.parseObject(JSONObject.toJSONString(svgNode));
    }

    /**
     * TODO 数据集  成图使用
     *
     * @param fileName
     * @return
     * @throws Exception
     */
    public static JSONObject findAllData(String fileName) throws Exception {
        JSONObject all = findAll(fileName);
        SvgNode svgNode = JSONObject.parseObject(JSONObject.toJSONString(all), SvgNode.class);

        postVisit(svgNode, null);
        inorderVisitClearACLine(svgNode, null);
        preVisit(svgNode, null);

        // 进行数据汇总,把环网柜组合在一起
        inorderVisitUnit(svgNode, null);


        initChildren(svgNode);
        FileUtil.writeString(JSONObject.toJSONString(svgNode, SerializerFeature.DisableCircularReferenceDetect), "C:\\svg\\data.xml", StandardCharsets.UTF_8);
        return JSONObject.parseObject(JSONObject.toJSONString(svgNode));
    }

    public static JSONObject findLinkData(String fileName) throws Exception {
        JSONObject all = findAll(fileName);
        SvgNode svgNode = JSONObject.parseObject(JSONObject.toJSONString(all), SvgNode.class);

        List<String> tttags = allNode.stream().filter(u -> ListUtil.of("海城金利钢结构工程有限公司", "海城市惠丰骨制品制造有限公司").contains(u.getContainerName())).map(SvgNode::getValue).collect(Collectors.toList());
        tags = ListUtil.list(false);
        tags.addAll(tttags);
        tags.add("PD_11000000_88415");
        tags.add("PD_11500000_94648");
        postVisit(svgNode, null);
        preVisit(svgNode, null);
        // 进行数据汇总,把环网柜组合在一起
        inorderVisitBusBar(svgNode, null);
        inorderVisitClearACLine(svgNode, null);
        initChildren(svgNode);
        FileUtil.writeString(JSONObject.toJSONString(svgNode, SerializerFeature.DisableCircularReferenceDetect), "C:\\svg\\link.xml", StandardCharsets.UTF_8);
        return JSONObject.parseObject(JSONObject.toJSONString(svgNode));
    }


    /**
     * TODO 环网详图
     *
     * @param fileNameList 多个文件
     * @return 树结构
     */
    public static JSONObject findAll(List<String> fileNameList) throws Exception {
        for (int i = 0; i < fileNameList.size(); i++) {
            findAll(fileNameList.get(i));
        }
        return null;
    }


    @Deprecated
    public static JSONObject findAll(String relativePath, String id) throws Exception {
        JSONObject jsonObject = XmlUtil.xmlJsonObj(FileUtil.readString(relativePath, StandardCharsets.UTF_8));
        List<JSONObject> list = ListUtil.list(false);
        jsonObject.keySet().forEach(u -> {
            List<JSONObject> data = ObjectToJsonList(jsonObject.get(u));
            list.addAll(data);
        });
        SvgNode p = SvgNode.builder().value("root").name("根").build();
        beginLoop(list, p, id);
        String s = JSONObject.toJSONString(p);
        return JSONObject.parseObject(s);

    }

    @Deprecated
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
        SvgNode p = SvgNode.builder().value("root").name("根").build();
        beginLoop(result, p, id);
        String s = JSONObject.toJSONString(p);
        return JSONObject.parseObject(s);
    }

    public static SvgNode findAllNode(String fileName) throws Exception {
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


        SvgNode p = SvgNode.builder().value("root").name("根").build();
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
    public static void beginLoop(List<JSONObject> list, SvgNode p, String id) {
        JSONObject o1 = first_findAndDelete(list, id);
        SvgNode x = SvgNode.builder().value(o1.getString(RDF_ID)).psrId(o1.getString(PSR_ID)).name(o1.getString(NAME)).type(o1.getString(TYPE)).containerId(o1.getString(CONTAINER_ID)).containerName(o1.getString(CONTAINER_NAME)).containerType(o1.getString(CONTAINER_TYPE)).build();
        List<JSONObject> o2 = second_findTerminals(list, id);
        o2.forEach(u -> {
            if (StrUtil.isNotEmpty(u.getString(CONNECTIVITY_NODE))) {
                SvgNode t = SvgNode.builder().value(u.getString(RDF_ID)).name("TERMINAL").type("TERMINAL").build();
                JSONObject o3 = third_findConnectivityNodes(list, u.getString(CONNECTIVITY_NODE));
                if (StrUtil.isNotEmpty(o3.getString(RDF_ID))) {
                    SvgNode c = SvgNode.builder().value(o3.getString(RDF_ID)).name("CONNECTIVITY_NODE").type("CONNECTIVITY_NODE").build();
                    List<JSONObject> o4 = forth_findTerminalsByConnectivityNode(list, o3.getString(RDF_ID));
                    o4.forEach(v -> beginLoop(list, c, v.getString(CONDUCTING_EQUIPMENT).replace("#", "")));
                    t.appendChild(c);
                }
                //直接接设备 删除连接点  t市terminal c是CONNECTIVITY_NODE
                if (t != null && t.getChildren() != null && t.getChildren().get(0) != null) {
                    x.appendChildren(t.getChildren().get(0).getChildren());
                }
                //x.appendChild(t);
            }
        });
        //站外-中压用户接入点:PD_37000000
        //站外-电缆终端头:PD_20200000
        if (x != null && x.getType() != null && !"Junction,EnergyConsumer,Assert".contains(x.getType())) {
            p.appendChild(x);
        }
        //p.appendChild(x);
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

    /**
     * TODO 1 查找指定的导电设备
     */
    public static JSONObject first_findAndDelete(List<JSONObject> list, String id) {
        JSONObject o = new JSONObject();
        Iterator<JSONObject> iterator = list.iterator();
        boolean continueFind = true;
        while (continueFind && iterator.hasNext()) {
            JSONObject u = iterator.next();
            if (u.get(RDF_ID).equals(id)) {
                iterator.remove();
                continueFind = false;
                o = u;
                LogsHandler.instance().logs("1找到并删除导电设备{}", u.getString(NAME));
            }
        }
        List<JSONObject> terPoleByTerminal = findPoleByTerminal(list, id);
        JSONObject object = terPoleByTerminal.stream().findFirst().orElse(null);

        JSONObject jsonObject = object != null ? object : o;
        //List<JSONObject> psrTypes = list.stream().filter(u -> Objects.equals(u.getString(TYPE), CIM_PSRTYPE)).collect(Collectors.toList());
        //String psrName = psrTypes.stream().filter(u -> Objects.equals(u.getString(RDF_ID), jsonObject.getString(PSRTYPE).replace("#", ""))).findFirst().get().getString(NAME);
        //System.out.println(psrName + "    -#-    " + jsonObject.getString(NAME));
        String containerName = findContainer(list, jsonObject.getString(CONTAINER)).stream().map(u -> u.getString(NAME)).findFirst().orElse("");
        String containerType = findContainer(list, jsonObject.getString(CONTAINER)).stream().map(u -> u.getString(TYPE)).findFirst().orElse("");
        jsonObject.put("CONTAINER_NAME", containerName);
        jsonObject.put("CONTAINER_TYPE", containerType);
        return jsonObject;
    }

    /**
     * TODO 2 通过导电设备的id 找到链接的端子 Terminal
     */
    public static List<JSONObject> second_findTerminals(List<JSONObject> list, String id) {

        List<JSONObject> l = ListUtil.list(false);
        Iterator<JSONObject> iterator = list.iterator();
        while (iterator.hasNext()) {
            JSONObject u = iterator.next();
            if ("Terminal".equals(u.getString(TYPE)) && u.get(RDF_ID).toString().contains(id)) {
                l.add(u);
                iterator.remove();
                LogsHandler.instance().logs("通过{}找到并删除端子{}", id, u.toString());
            }
        }
        return l;
    }

    /**
     * TODO 3 通过端子的connectivityNode 找到连接点
     */
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

    /**
     * TODO 4通过连接点的id 找到所有关联的端子
     */
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

    /**
     * TODO 通过Terminal的 对应 杆塔的 sgcim:PoleCode.Terminal 找到杆塔
     */
    public static List<JSONObject> findPoleByTerminal(List<JSONObject> list, String terminal) {
        List<JSONObject> f = ListUtil.list(false);
        Iterator<JSONObject> iterator = list.iterator();
        while (iterator.hasNext()) {
            JSONObject u = iterator.next();
            if (StrUtil.isNotEmpty(u.getString(POLECODETERMINAL)) && u.getString(POLECODETERMINAL).contains(terminal)) {
                f.add(u);
                iterator.remove();
                LogsHandler.instance().logs("通过{}找到并删除杆塔{}", terminal, u.toString());
            }
        }
        return f;
    }

    /**
     * TODO  通过Disconnector(开关)||BusbarSection（母线）的Equipment.EquipmentContainer  对应 Substation（开闭站） IdentifiedObject.mRID 找到所属变电站 环网柜
     */
    public static List<JSONObject> findContainer(List<JSONObject> list, String container) {
        if (StrUtil.isEmpty(container)) {
            return ListUtil.list(false);
        }
        list = list.stream().filter(v -> !Objects.equals(v.getString(TYPE), CIM_PSRTYPE) && !Objects.equals(v.getString(TYPE), CIM_VOLTAGELEVEL)).collect(Collectors.toList());
        List<JSONObject> s = ListUtil.list(false);
        Iterator<JSONObject> iterator = list.iterator();
        while (iterator.hasNext()) {
            JSONObject u = iterator.next();
            if (StrUtil.isNotEmpty(u.getString(RDF_ID)) && container.contains(u.getString(RDF_ID).replace("PD_", "").replace("#", ""))) {
                s.add(u);
                //iterator.remove();
                //LogsHandler.instance().logs("通过{}找到并删除变电站{}", container, u.toString());
            }
        }
        return s;
    }


    //findFileName
    public static String findFileName(String rdfId) {
        return "anshan_高新区_汤岗子线单线图.sln.xml";
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
    public static void findNode(SvgNode node, List<String> brakers) {
        if (node == null || brakers.contains(node.getValue())) {
            return;
        }
        if (node.getChildren() == null) {
            SvgNode rs = SvgNode.builder().name(node.getName()).value(node.getValue()).build();
            nodes.add(rs);
            return;
        }
        SvgNode rs = SvgNode.builder().name(node.getName()).value(node.getValue()).build();
        nodes.add(rs);
        node.getChildren().forEach(u -> {
            findNode(u, brakers);
        });

    }

    /**
     * TODO 根据节点名称获取节点
     *
     * @param node    : 节点
     * @param breaker : 开关id
     * @return void
     * @author xuhong.ding
     * @since 2022/4/21 14:24
     **/
    public static void findNode(SvgNode node, String breaker) {
        if (node == null || node.getChildren() == null) {
            return;
        }
        if (node.getValue().equals(breaker)) {
            firstNode = node;
            return;
        }
        node.getChildren().forEach(u -> {
            findNode(u, breaker);
        });
    }

    static boolean continueVisit = false;
    /* static List<String> tags = ListUtil.of("PD_30700000_763820", "PD_31100000_306157", "PD_30700000_763821", "PD_30700000_763822", "PD_30200002_341983" //辽宁省军区教导队11071 环网柜
             , "PD_30700000_1217977", "PD_31100000_539606", "PD_30700000_1217978", "PD_30700000_1218020", "PD_30700000_1218019", "PD_30700000_1217979", "PD_30700000_1218018" //金属线千山区消防救援大队环网柜
             , "PD_30700000_1218006", "PD_31100000_539667", "PD_30700000_1218007" //金属线千山区消防救援大队环网柜
             , "PD_30700000_1223435", "PD_31100000_542458", "PD_30700000_1223434", "PD_30700000_1223433" //用户变电站10106
             , "PD_30200002_619103", "PD_30700000_1223837", "PD_30200002_619104", "PD_30700000_1223838", "PD_30200002_619105", "PD_30700000_1223839", "PD_30200002_619102", "PD_30700000_1223836", "PD_30200002_619108", "PD_30700000_1223815", "PD_30200002_619109", "PD_30700000_1223856", "PD_30700000_1223857", "PD_30200002_619106", "PD_30700000_1223840", "PD_30200002_619107", "PD_30700000_1223841", "PD_31100000_542818", "PD_30700000_1223814", "PD_31100000_542817", "PD_30700000_1223755" //方舱医院
             , "PD_30700000_1223353", "PD_30700000_1223351", "PD_30700000_1223352", "PD_30700000_1223354", "PD_30700000_1223355", "PD_31100000_542425", "PD_30700000_1223350" //10kV解康线健康驿站环网柜
     );*/
    static List<String> tags = ListUtil.list(false);
    static List<String> saveTypes = ListUtil.of("Substation", "Breaker", "BusbarSection");


    /**
     * TODO 后序遍历 删除多余数据
     *
     * @param node
     * @param pNode
     */
    public static void postVisit(SvgNode node, SvgNode pNode) {
        if (node == null) {
            return;
        }
        List<SvgNode> children = node.getChildren();
        children = children == null ? ListUtil.list(false) : children;
        final CopyOnWriteArrayList<SvgNode> cowList = new CopyOnWriteArrayList<>(children);
        for (int i = 0; i < cowList.size(); i++) {
            postVisit(cowList.get(i), node);
        }
        allNode.add(node);
        if (tags.size() > 0 && pNode != null && !tags.contains(node.getValue()) && (node.getChildren() == null || node.getChildren().size() == 0)) {
            pNode.removeChild(node);
        }
    }

    /**
     * TODO 前序遍历 从根节点开始 删除多余相邻节点
     */
    public static void preVisit(SvgNode node, SvgNode pNode) {
        if (continueVisit && node == null) {
            return;
        }
        List<SvgNode> children = node.getChildren();
        children = children == null ? ListUtil.list(false) : children;
        if (tags.size() > 0 && !saveTypes.contains(node.getType())     // 不在需要保留的设备范围内
                && (node.getChildren() != null && node.getChildren().size() < 2) //不是分叉节点（分叉不大于1的节点）
                && !tags.contains(node.getValue()) //  不在新增设备范围之内
                && (node.getChildren().stream().noneMatch(u -> tags.contains(u.getValue())))) { //子节点没有新增节点
            continueVisit = true;
            pNode.removeChild(node);
            pNode.appendChildren(children);
        }
        SvgNode tpNode = node;
        if (continueVisit) {
            tpNode = pNode;
            continueVisit = false;
        }

        children = node.getChildren();
        children = children == null ? ListUtil.list(false) : children;
        final CopyOnWriteArrayList<SvgNode> cowList = new CopyOnWriteArrayList<SvgNode>(children);

        for (SvgNode svgNode : cowList) {
            preVisit(svgNode, tpNode);
        }
    }


    public static void initChildren(SvgNode node) {
        node.setId(node.getValue());

        List<SvgNode> children = node.getChildren();
        if (children == null) {
            return;
        }
        children.forEach(u -> {
            u.setParentId(node.getId());
            initChildren(u);
        });

    }

    /**
     * TODO 中序遍历 构建环网柜  进行数据汇总,把环网柜组合在一起
     */
    public static void inorderVisitUnit(SvgNode node, SvgNode pNode) {
        if (node == null) {
            return;
        }
        List<SvgNode> children = node.getChildren();
        children = children == null ? ListUtil.list(false) : children;
        final CopyOnWriteArrayList<SvgNode> cowList = new CopyOnWriteArrayList<SvgNode>(children);
        if (cowList.size() > 1 && node.getContainerType().equals("Substation")) {

            if (node.getContainerName().equals(pNode.getName())) {  //双母线 变电站名称一致 直接连母线
                SvgNode busbarSection = cowList.stream().filter(u -> u.getType().equals("BusbarSection")).findFirst().get(); //取出母线
                cowList.remove(busbarSection); //移除母线
                busbarSection.appendChild(node); //母线节点上移
                busbarSection.appendChildren(cowList);//母线节点上移
                pNode.removeChild(node);//删除原来子节点 连接变电站
                pNode.appendChild(busbarSection);
                node.setChildren(ListUtil.list(false));
                node = busbarSection;
            } else {
                SvgNode busbarSection = cowList.stream().filter(u -> u.getType().equals("BusbarSection")).findFirst().get(); //取出母线
                cowList.remove(busbarSection); //移除母线
                // 构建变电站节点
                String string = UUID.randomUUID().toString();
                SvgNode subStation = SvgNode.builder().id(string).value(string).containerName(pNode.getContainerName()).containerId(pNode.getContainerId()).type(node.getContainerType()).name(node.getContainerName()).build();
                busbarSection.appendChild(node); //母线节点上移
                busbarSection.appendChildren(cowList);//母线节点上移
                subStation.appendChild(busbarSection);//变电站节点上移
                pNode.removeChild(node);//删除原来子节点 连接变电站
                pNode.appendChild(subStation);
                node.setChildren(ListUtil.list(false));
                node = subStation;
            }
        }
        for (SvgNode svgNode : cowList) {
            inorderVisitUnit(svgNode, node);
        }
    }

    /**
     * TODO 中序遍历 构建环网柜  进行数据汇总,把母线提上来
     */
    public static void inorderVisitBusBar(SvgNode node, SvgNode pNode) {
        if (node == null) {
            return;
        }
        List<SvgNode> children = node.getChildren();
        children = children == null ? ListUtil.list(false) : children;
        final CopyOnWriteArrayList<SvgNode> cowList = new CopyOnWriteArrayList<SvgNode>(children);
        if (cowList.size() > 1 && node.getContainerType().equals("Substation")) {
            SvgNode busbarSection = cowList.stream().filter(u -> u.getType().equals("BusbarSection")).findFirst().get(); //取出母线
            cowList.remove(busbarSection); //移除母线
            busbarSection.appendChildren(cowList);//母线节点上移
            node.setChildren(ListUtil.list(false));
            node.appendChild(busbarSection);
        }
        for (SvgNode svgNode : cowList) {
            inorderVisitBusBar(svgNode, node);
        }
    }

    public static void inorderVisitClearACLine(SvgNode node, SvgNode pNode) {
        if (node == null) {
            return;
        }
        List<SvgNode> children = node.getChildren();
        children = children == null ? ListUtil.list(false) : children;
        final CopyOnWriteArrayList<SvgNode> cowList = new CopyOnWriteArrayList<SvgNode>(children);
        if (node.getType().equals("ACLineSegment")) {
            pNode.removeChild(node);
            pNode.appendChildren(node.getChildren());
            node = cowList.size() > 0 ? cowList.get(0) : null;
        }
        for (SvgNode svgNode : cowList) {
            inorderVisitClearACLine(svgNode, node);
        }
    }

    /**
     * TODO 前后相连
     *
     * @param node
     */
    static void getLinkNode(SvgNode node) {
        if (node == null) {
            return;
        }
        List<SvgNode> children = node.getChildren();
        children = children == null ? ListUtil.list(false) : children;
        for (int i = 0; i < children.size(); i++) {
            svgLinkModelList.add(SvgLinkModel.builder().beginNode(node.getId()).beginName(node.getName())
                    .endNode(children.get(i).getId()).endName(children.get(i).getName()).build());
            System.out.println(node.getName() + children.get(i).getName());
            getLinkNode(children.get(i));
        }
    }

}
