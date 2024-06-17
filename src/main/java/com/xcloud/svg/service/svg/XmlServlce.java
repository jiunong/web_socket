package com.xcloud.svg.service.svg;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSONObject;
import com.xcloud.svg.pojo.SvgNode;
import com.xcloud.svg.pojo.PoleTransformer;
import com.xcloud.svg.pojo.PsrType;
import com.xcloud.svg.util.XmlUtil;
import org.dom4j.DocumentException;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static com.xcloud.svg.service.svg.SvgService.*;

/**
 * TODO
 *
 * @author xuhong.ding
 * @since 2022/5/11 10:53
 */
public class XmlServlce {

    //private static List<String> searchTypes = ListUtil.of("PD_30000004","PD_30000005","PD_30000006","PD_20400000");
    private static List<String> searchTypes = ListUtil.of("PD_20900000","PD_37000000");

    public static List<PoleTransformer> getPoles(String relativePath) throws DocumentException {
        JSONObject object = XmlUtil.xmlJsonObj(FileUtil.readString(relativePath, StandardCharsets.UTF_8));
        List<SvgNode> onPoles = ListUtil.list(false);
        List<SvgNode> offPoles = ListUtil.list(false);
        List<JSONObject> list = ObjectToJsonList(object.get(POLECODE));
        List<JSONObject> powers = ObjectToJsonList(object.get(POWERTRANSFORMER));//所有的变压器
        List<JSONObject> breakers = ObjectToJsonList(object.get(BREAKER));
        list.forEach(u -> {
            if (u.get(RDF_ID).equals("PD_10200000_2931785")) {
                System.out.println();
            }
            SvgNode pole = SvgNode.builder().name(u.getString(NAME)).value(u.getString(RDF_ID)).build();
            List<Object> o = ObjectToJsonList(u).stream().map(m -> m.get("PoleCode.Terminal")).collect(Collectors.toList());
            List<Object> ts = ListUtil.list(false);
            if (o.get(0) instanceof String) {
                ts.add(o.get(0));
            } else {
                ts = (List<Object>) o.get(0);
            }

            List<SvgNode> cl = ListUtil.list(false);
            for (int i = 0; i < ts.size(); i++) {
                String s = ts.get(i).toString();
                String substring = s.substring(s.indexOf("#") + 1, s.length() - 2);
                JSONObject jsonObject = powers.stream().filter(w -> w.getString(RDF_ID).equals(substring)).findFirst().orElse(null);
                if (jsonObject != null) {
                    SvgNode power = SvgNode.builder().name(jsonObject.getString(NAME)).value(jsonObject.getString(RDF_ID)).build();
                    cl.add(power);
                }
            }
            pole.setChildren(cl);
            if (cl.size() > 0) {
                onPoles.add(pole);
            } else {
                offPoles.add(pole);
            }
        });
        List<PoleTransformer> allPowers = ListUtil.list(false);
        List<String> allPowerIds = ListUtil.list(false);
        onPoles.forEach(u -> {
            List<SvgNode> children = u.getChildren();
            children.forEach(v -> {
                PoleTransformer poleTransformer = PoleTransformer.builder().transformerName(v.getName()).transformerId(v.getValue()).poleId(u.getValue()).poleName(u.getName()).build();
                allPowers.add(poleTransformer);
                allPowerIds.add(v.getValue());
            });
        });

        powers.forEach(u -> {
            if (!allPowerIds.contains(u.getString(RDF_ID))) {
                PoleTransformer poleTransformer = PoleTransformer.builder().transformerId(u.get(RDF_ID).toString()).transformerName(u.getString(NAME)).build();
                allPowers.add(poleTransformer);
            }
        });
        return allPowers;
    }

    public static List<PsrType> getPsrType(String relativePath) throws Exception {
        HashSet<PsrType> allTypes = CollectionUtil.set(false);
        relativePath = "G:\\programSoft\\tomcat-all\\tomcat-all\\apache-tomcat-7.0.96\\webapps\\omspdjx_upload\\svg";
        List<File> files = FileUtil.loopFiles(relativePath, u -> u.getName().endsWith(".xml"));
        //遍历文件集合，转成json对象
        files.forEach(u -> {
            try {
                JSONObject object = XmlUtil.xmlJsonObj(FileUtil.readString(u, StandardCharsets.UTF_8));
                List<JSONObject> psts = ObjectToJsonList(object.get(CIM_PSRTYPE));
                psts.forEach(v -> {
                    if (searchTypes.contains(v.get(RDF_ID))) {
                        System.out.println(u.getAbsolutePath()+"\t::::"+v.getString(RDF_ID));
                    }
                    PsrType build = PsrType.builder().psrTypeName(v.getString(NAME))
                            .psrTypeId(v.getString(RDF_ID)).build();
                    allTypes.add(build);
                });
            } catch (DocumentException e) {
                //System.out.println(u.getName()+"转换失败");
            }finally {
                //System.out.println(u.getName()+"转换成功");
            }
        });
        List<PsrType> collect = allTypes.stream().sorted(Comparator.comparing(PsrType::getPsrTypeId)).collect(Collectors.toList());
        return collect;
    }
}
