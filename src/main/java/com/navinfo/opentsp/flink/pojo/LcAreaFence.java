package com.navinfo.opentsp.flink.pojo;

public class LcAreaFence {
    private Long areaId;

    private Integer areaType;

    private Long lastUpdateTime;

    private Integer delFlag;

    private String areaData;

    private Integer areaProp;

    public Integer getAreaProp() {
        return areaProp;
    }

    public void setAreaProp(Integer areaProp) {
        this.areaProp = areaProp;
    }

    public Long getAreaId() {
        return areaId;
    }

    public void setAreaId(Long areaId) {
        this.areaId = areaId;
    }

    public Integer getAreaType() {
        return areaType;
    }

    public void setAreaType(Integer areaType) {
        this.areaType = areaType;
    }

    public Long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public Integer getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(Integer delFlag) {
        this.delFlag = delFlag;
    }

    public String getAreaData() {
        return areaData;
    }

    public void setAreaData(String areaData) {
        this.areaData = areaData == null ? null : areaData.trim();
    }
}