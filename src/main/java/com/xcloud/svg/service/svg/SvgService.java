package com.xcloud.svg.service.svg;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.xcloud.svg.util.XmlUtil;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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
    public static final String CONNECTIVITY_NODE = "Terminal.ConnectivityNode";
    public static final String PSRTYPE = "PowerSystemResource.PSRType";
    public static final String CONTAINER = "Equipment.EquipmentContainer";
    public static final String POLECODE = "PoleCode";
    public static final String TERMINAL = "Terminal";
    public static final String POLECODETERMINAL = "PoleCode.Terminal";

    public static void main(String[] args) throws Exception {
        findAll();
    }

    public static String findAll() throws Exception {
        String relativePath = "C:\\svg\\113疏豪线单线图.sln.xml";
        JSONObject jsonObject = XmlUtil.xmlJsonObj(FileUtil.readString(relativePath, StandardCharsets.UTF_8));
        HashMap<String, List<JSONObject>> map = MapUtil.newHashMap(false);
        List<JSONObject> list = ListUtil.list(false);
        jsonObject.keySet().forEach(u -> {
            List<JSONObject> data = ObjectToJsonList(jsonObject.get(u));
            map.put(u, data);
            list.addAll(data);
        });
        System.out.println(map.size());
        Node p = Node.builder().value("@@@@").name("根").build();
        String id = "PD_31100000_288122";
        begin2(list, p, id);
        String s = JSONObject.toJSONString(p);
        return s;
        /* System.out.println("处理后数据长度为"+list.size());*/

    }

    public static void begin(List<JSONObject> list, Node p, String id) {

        JSONObject o1 = first_findAndDelete(list, id);

        Node x = Node.builder().value(o1.getString(MRID)).name(o1.getString(NAME)).build();
        List<JSONObject> o2 = second_findTerminals(list, id);
        o2.forEach(u -> {
            if (StrUtil.isNotEmpty(u.getString(CONNECTIVITY_NODE))) {
                Node t = Node.builder().value(u.getString(MRID)).name("TERMINAL").build();
                JSONObject o3 = third_findConnectivityNodes(list, u.getString(CONNECTIVITY_NODE));
                if (StrUtil.isNotEmpty(o3.getString(RDF_ID))) {
                    Node c = Node.builder().value(o3.getString(MRID)).name("CONNECTIVITY_NODE").build();
                    List<JSONObject> o4 = forth_findTerminalsByConnectivityNode(list, o3.getString(RDF_ID));
                    o4.forEach(v -> {
                        begin(list, c, v.getString(CONDUCTING_EQUIPMENT).replace("#", ""));
                    });
                    t.appendChild(c);
                }
                x.appendChild(t);
            }
        });
        p.appendChild(x);
    }

    public static void begin2(List<JSONObject> list, Node p, String id) {

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
                        begin2(list, c, v.getString(CONDUCTING_EQUIPMENT).replace("#", ""));
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
        } else {
            list.addAll((List<JSONObject>) o);
        }
        return list;
    }

    //查找指定的导电设备
    public static JSONObject first_findAndDelete(List<JSONObject> list, String id) {
        JSONObject o = new JSONObject();
        Iterator<JSONObject> iterator = list.iterator();
        while (iterator.hasNext()) {
            JSONObject u = iterator.next();
            if (u.get(RDF_ID).equals(id)) {
                iterator.remove();
                o = u;
/*
                LogsHandler.instance().logs("1找到并删除导电设备{}", u.toString());
*/
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
/*
                LogsHandler.instance().logs("通过{}找到并删除端子{}", id, u.toString());
*/
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
/*
                LogsHandler.instance().logs("1找到并删除连接点{}", u.toString());
*/
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
/*
                LogsHandler.instance().logs("通过{}找到并删除端子{}", connectivityNode, u.toString());
*/
            }
        }
        return l;
    }
    //5 通过端子的 ConductingEquipment 寻找指定的导电设备 回到1
}
