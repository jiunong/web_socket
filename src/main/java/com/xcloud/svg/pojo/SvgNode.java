package com.xcloud.svg.pojo;

import cn.hutool.core.collection.ListUtil;
import com.xcloud.svg.service.svg.SvgService;
import lombok.*;

import javax.swing.plaf.PanelUI;
import java.util.Iterator;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SvgNode {

    private List<SvgNode> children;
    private String value;
    private String id;
    private String psrId;
    private String parentId;
    private String name;
    private String type;
    private String containerId;
    private String containerName;
    private String containerType;

    public void appendChild(SvgNode n) {
        if (this.children == null) {
            this.children = ListUtil.list(false);
        }
        this.children.add(n);
    }

    public void appendChildren(List<SvgNode> ns) {
        if (this.children == null) {
            this.children = ListUtil.list(false);
        }
        if (ns != null) {
            this.children.addAll(ns);
        }
    }

    public void removeChild(SvgNode nm) {
        this.children.remove(nm);
    }

    /**
     * @param tags      重要设备
     * @param saveTypes 保存的设备类型
     */
    public void removeInvalidChildren(List<String> tags, List<String> saveTypes) {
        Iterator<SvgNode> it = children.iterator();
        while (it.hasNext()) {
            SvgNode child = it.next();
            if (!saveTypes.contains(child.getType())
                    && (child.getChildren() != null && child.getChildren().size() < 2)
                    && !tags.contains(child.getValue())
                    && (child.getChildren().stream().noneMatch(u -> tags.contains(u.getValue())))) {
                it.remove();
            } else {
                child.removeInvalidChildren(tags, saveTypes); // 递归删除子节点中不符合条件的节点
                if (child.children.isEmpty()) {
                    it.remove(); // 如果子节点为空，则删除这个节点
                }
            }
        }
    }

    public static SvgNode root(List<SvgNode> children) {
        return SvgNode.builder().value("root").name("root").type("root").children(children).build();
    }

    public static SvgNode busbarSection() {
        return SvgNode.builder().id("UUIDUtil.getUpperUUID()").value("母线").name("母线").psrId("UUIDUtil.getUpperUUID()").type(SvgService.BUSBARSECTION).build();
    }

}
