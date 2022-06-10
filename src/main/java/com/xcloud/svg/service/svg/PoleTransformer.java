package com.xcloud.svg.service.svg;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * TODO
 *
 * @author xuhong.ding
 * @since 2022/5/11 8:51
 */
@Data
@EqualsAndHashCode
@Builder
public class PoleTransformer {
    @ExcelProperty("transformerId")
    private String transformerId;

    @ExcelProperty("transformerName")
    private String transformerName;

    @ExcelProperty("poleId")
    private String poleId;

    @ExcelProperty("poleName")
    private String poleName;
}
