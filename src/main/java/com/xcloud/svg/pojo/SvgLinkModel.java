package com.xcloud.svg.pojo;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SvgLinkModel {

    private String beginNode;
    private String beginName;
    private String endNode;
    private String endName;
    @Builder.Default
    private String linkType = "ACLineSegment";


}
