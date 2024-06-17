package com.xcloud.svg.pojo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Objects;

/**
 * TODO
 *
 * @author xuhong.ding
 * @since 2022/5/11 14:32
 */
@Data
@Builder
@EqualsAndHashCode
public class PsrType {
    @ExcelProperty(value = "类型", index = 0)
    private String psrTypeId;
    @ExcelProperty(value = "类型名称", index = 1)
    private String psrTypeName;

}
