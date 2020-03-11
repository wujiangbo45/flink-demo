package com.navinfo.opentsp.flink.pojo;


import java.io.Serializable;
import java.util.concurrent.CopyOnWriteArrayList;

public class AreaFenceInfo implements Serializable{


    private long areaId;//区域标识

    private int areaType;//区域形状类型

    private CopyOnWriteArrayList<AreaDataEntity> areaData;//区域数据JSON

    private long lastUpdateTime;//最后修改时间

    private int areaProp;

    private int type;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getAreaProp() {
        return areaProp;
    }

    public void setAreaProp(int areaProp) {
        this.areaProp = areaProp;
    }

    public long getAreaId() {
        return areaId;
    }

    public void setAreaId(long areaId) {
        this.areaId = areaId;
    }

    public int getAreaType() {
        return areaType;
    }

    public void setAreaType(int areaType) {
        this.areaType = areaType;
    }

    public CopyOnWriteArrayList<AreaDataEntity> getAreaData() {
        return areaData;
    }

    public void setAreaData(CopyOnWriteArrayList<AreaDataEntity> areaData) {
        this.areaData = areaData;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

}
