package com.navinfo.opentsp.flink.pojo;

import java.io.Serializable;

/**
 * @Author: wujiangbo
 * @Create: 2019/06/06 下午1:51
 */
public class AreaFenceWhiteList implements Serializable {

    private Long areaId;

    private Long terminalId;

    public Long getAreaId() {
        return areaId;
    }

    public void setAreaId(Long areaId) {
        this.areaId = areaId;
    }

    public Long getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(Long terminalId) {
        this.terminalId = terminalId;
    }
}
