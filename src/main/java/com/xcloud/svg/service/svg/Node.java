package com.xcloud.svg.service.svg;

import cn.hutool.core.collection.ListUtil;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Node {

    private List<Node> children;
    private String value;
    private String name;
    private String type;
    private String containerName;

    public void appendChild(Node n) {
        if (this.children == null) {
            this.children = ListUtil.list(false);
        }
        this.children.add(n);
    }

    public void appendChildren(List<Node> ns) {
        if (this.children == null) {
            this.children = ListUtil.list(false);
        }
        if (ns != null) {
            this.children.addAll(ns);
        }

    }

}
