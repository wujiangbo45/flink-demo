package com.navinfo.opentsp.flink.mapper;



import com.navinfo.opentsp.flink.pojo.AreaFenceWhiteList;
import com.navinfo.opentsp.flink.pojo.LcAreaFence;

import java.util.List;

public interface LcAreaFenceMapper {

    List<LcAreaFence> selectAll();

    List<AreaFenceWhiteList> selectWhiteList();
}