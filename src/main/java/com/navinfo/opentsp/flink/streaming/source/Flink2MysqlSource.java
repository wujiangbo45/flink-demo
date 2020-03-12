package com.navinfo.opentsp.flink.streaming.source;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.navinfo.opentsp.flink.mapper.LcAreaFenceDaoImpl;
import com.navinfo.opentsp.flink.mapper.LcAreaFenceMapper;
import com.navinfo.opentsp.flink.pojo.AreaDataEntity;
import com.navinfo.opentsp.flink.pojo.AreaFenceInfo;
import com.navinfo.opentsp.flink.pojo.LcAreaFence;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class Flink2MysqlSource extends RichSourceFunction<List<AreaFenceInfo>> {
    private static final Logger logger = LoggerFactory.getLogger(Flink2MysqlSource.class);


    @Override
    public void run(SourceContext ctx) {
        try {
            LcAreaFenceMapper areaFenceMapper = new LcAreaFenceDaoImpl();
            // 围栏数据
            // 白名单数据
            while (true) {
                List<LcAreaFence> list = areaFenceMapper.selectAll();

                // 所有电子围栏数据
                List<AreaFenceInfo> collect = list.stream().
                        map(lcAreaFence -> {
                            AreaFenceInfo info = new AreaFenceInfo();
                            info.setAreaId(lcAreaFence.getAreaId());
                            info.setAreaType(lcAreaFence.getAreaType());
                            info.setLastUpdateTime(lcAreaFence.getLastUpdateTime());
                            info.setAreaProp(lcAreaFence.getAreaProp());
                            CopyOnWriteArrayList<AreaDataEntity> areaDataEntities =
                                    JSON.parseObject(lcAreaFence.getAreaData(), new TypeReference<CopyOnWriteArrayList<AreaDataEntity>>() {
                                    });
                            info.setAreaData(areaDataEntities);
                            return info;
                        }).collect(Collectors.toList());
                ctx.collect(collect);
                Thread.sleep(60000);
                }
            } catch(InterruptedException e){
                e.printStackTrace();
            }
    }

    @Override
    public void cancel() {

    }
}
