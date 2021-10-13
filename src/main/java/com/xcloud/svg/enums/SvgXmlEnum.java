package com.xcloud.svg.enums;

import cn.hutool.core.util.StrUtil;

import java.sql.Struct;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * TODO xml模文件标签
 *
 * @author xuhong.ding
 * @since 2020/9/28 15:36
 */
public enum SvgXmlEnum {

    /*图源标签*/
    /*ASSET("Asset", ""),*/
    /**
     * 导线
     */
//    ACLINE_SEGMENT("ACLineSegment", "#PD_10100000"),
//    ACLINE_SEGMENT1("ACLineSegment", "#PD_20100000"),

//    BASE_VOLTAGE("BaseVoltage", ""),

    /**
     * 断路器
     */
    BREAKER("Breaker", "#PD_11100000"),
    BREAKER1("Breaker", "#PD_11400000"),
    BREAKER2("Breaker", "#PD_30500000"),

    /**
     * 母线
     */
//    BUSBAR_SECTION("BusbarSection", "#PD_31100000"),
//    CURRENT_TRANSFORMER("CurrentTransformer", "#PD_31300001"),
//    CONNECTIVITY_NODE("ConnectivityNode", ""),

    /**
     * 刀闸
     */
/*    DISCONNECTOR("Disconnector", "#PD_11300000"),
    DISCONNECTOR1("Disconnector", "#PD_30600000"),
    DISCONNECTOR2("Disconnector", "#PD_30600003"),
    DISCONNECTOR3("Disconnector", "#PD_30600005"),*/
    /**
     * 间隔熔断器
     */
    FUSE("Fuse", "#PD_11500000"),
    FUSE1("Fuse", "#PD_11500001"),
    FUSE2("Fuse", "#PD_11500002"),
    FUSE3("Fuse", "#PD_30900000"),
    FUSE4("Fuse", "#PD_30900002"),

    /**
     * 馈线
     */
//    FEEDER("Feeder", "#PD_10000100"),
//    GEOGRAPHICAL_REGION("GeographicalRegion", ""),

    /**
     * 接地刀闸
     */
//    GROUND_DISCONNECTOR("GroundDisconnector", "#PD_30600001"),

    /**
     * 间隔
     */
//    JUNCTION("Junction", "#PD_20200000"),
//    JUNCTION1("Junction", "#PD_20300000"),
//    JUNCTION2("Junction", "#PD_32000000"),

    /**
     * 负荷开关
     */
    LOAD_BREAK_SWITCH("LoadBreakSwitch", "#PD_11200000"),
    LOAD_BREAK_SWITCH1("LoadBreakSwitch", "#PD_30700000"),

//    POLE_CODE("PoleCode", "#PD_10200000"),
//    POLE_CODE1("PoleCode", "#PD_10200001"),

    /**
     * 变压器
     */
    POWER_TRANSFORMER("PowerTransformer", "#PD_11000000"),
    POWER_TRANSFORMER1("PowerTransformer", "#PD_11000001"),
    POWER_TRANSFORMER2("PowerTransformer", "#PD_11000002"),
    POWER_TRANSFORMER3("PowerTransformer", "#PD_30100002"),
    POWER_TRANSFORMER4("PowerTransformer", "#PD_30200002"),
    POWER_TRANSFORMER5("PowerTransformer", "#PD_30200003"),
    POWER_TRANSFORMER6("PowerTransformer", "#PD_30200004"),
    POWER_TRANSFORMER7("PowerTransformer", "#PD_30300000");

//    PSR_TYPE("PSRType", ""),
//    POWER_TRANSFORMER_END("PowerTransformerEnd", ""),
//    REMOTE_UNIT("RemoteUnit", ""),
//    SUB_GEOGRAPHICAL_REGION("SubGeographicalRegion", ""),
//
//    SERVICE_LOATION("ServiceLoation", "PD_37100000"),

    /**
     * 开闭站等站点
     */
   /* SUBSTATION("Substation", "#PD_20400000"),
    SUBSTATION1("Substation", "#PD_30000000"),
    SUBSTATION2("Substation", "#PD_30000004"),
    SUBSTATION3("Substation", "#PD_30000005"),
    SUBSTATION4("Substation", "#PD_30000006"),
    SUBSTATION5("Substation", "#PD_32300000"),
    SUBSTATION6("Substation", "#PD_32400000"),
    SUBSTATION7("Substation", "#PD_34300000"),
*/
    /**
     * 避雷器
     */
//    SURGEARRESTER("SurgeArrester", "#PD_31800001"),
//    SURGEARRESTER1("SurgeArrester", "#PD_11600000"),
//    SURGEARRESTER2("SurgeArrester", "#PD_31800000"),
//
//    ENERGYCONSUMER("EnergyConsumer", "#PD_37000000"),
//
//    TERMINAL("Terminal", ""),
//    USAGE_POINT("UsagePoint", "PD_37200000"),
    /**
     * 电压等级：exp交流10kV
     */
//    VOLTAGE_LEVEL("VoltageLevel", "");


    private String label;
    private String psrType;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getPsrType() {
        return psrType;
    }

    public void setPsrType(String psrType) {
        this.psrType = psrType;
    }

    SvgXmlEnum(String label, String psrType) {
        this.label = label;
        this.psrType = psrType;
    }

    SvgXmlEnum() {
    }

    /**
     * TODO 根据psrType获取对应Label
     *
     * @param psrType psrType
     * @return java.lang.String
     * @author xuhong.ding
     * @since 2020/9/29 9:08
     */
    public static String getLabelByPsrType(String psrType) {
        String newPsrType = Optional.ofNullable(psrType).map(v -> {
            return v.startsWith("#") ? v : "#".concat(v);
        }).map(String::toString).orElse("null");
        return Arrays.asList(SvgXmlEnum.values()).stream()
                .filter(u -> u.getPsrType().equals(newPsrType))
                .map(SvgXmlEnum::getLabel)
                .findFirst().orElse(null);
    }

    public static Set<String> getLabels() {
        return Arrays.stream(SvgXmlEnum.values())
                .map(SvgXmlEnum::getLabel)
                .collect(Collectors.toSet());
    }

    public static boolean in(String label) {
        if (StrUtil.isEmpty(label)) return false;
        return getLabels().contains(label);
    }

}

