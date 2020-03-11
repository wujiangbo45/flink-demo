package com.navinfo.opentsp.flink.streaming.transformation;

import com.navinfo.opentsp.flink.pojo.AreaFenceInfo;
import com.navinfo.opentsp.flink.streaming.source.Flink2MysqlSource;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.state.BroadcastState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.state.HeapBroadcastState;
import org.apache.flink.streaming.api.datastream.BroadcastStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.BroadcastProcessFunction;
import org.apache.flink.streaming.api.functions.co.CoProcessFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FlinkLoadMysqlSource {
    private static final Logger logger = LoggerFactory.getLogger(FlinkLoadMysqlSource.class);
    private static Map<Long,AreaFenceInfo> areaFenceInfoMap = new HashMap<>();

    // 加载mysql数据,并且将数据广播出去,固定时间加载一次
    public static void main(String[] args) throws Exception {
        final MapStateDescriptor<String, Map<Long,AreaFenceInfo>> CONFIG_DESCRIPTOR = new MapStateDescriptor<>(
                "wordsConfig",
                BasicTypeInfo.STRING_TYPE_INFO,
                Types.MAP(Types.LONG, Types.POJO(AreaFenceInfo.class)));
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        BroadcastStream<Map<Long, AreaFenceInfo>> broadcast = env.addSource(new Flink2MysqlSource())
                .setParallelism(1).map(value -> value.stream().collect(Collectors.toMap(AreaFenceInfo::getAreaId, areaFenceInfo1 -> areaFenceInfo1))).
                        returns(Types.MAP(Types.LONG,Types.POJO(AreaFenceInfo.class)))
                        .broadcast(CONFIG_DESCRIPTOR);
        DataStreamSource<Long> integerDataStreamSource = env.fromElements(1L, 3L, 4L, 1000L, 5555L);
        integerDataStreamSource.setBufferTimeout(5000L).connect(broadcast).process(new BroadcastProcessFunction<Long, Map<Long, AreaFenceInfo>, Object>() {

            boolean loadFinish;

            @Override
            public void processElement(Long value, ReadOnlyContext ctx, Collector<Object> out) throws Exception {
                HeapBroadcastState<String,Map<Long,AreaFenceInfo>> config = (HeapBroadcastState)ctx.getBroadcastState(CONFIG_DESCRIPTOR);
                Map<Long, AreaFenceInfo> area_fence_info = config.get("area_fence_info");
                System.out.println(area_fence_info != null);

            }

            @Override
            public void processBroadcastElement(Map<Long, AreaFenceInfo> value, Context ctx, Collector<Object> out) throws Exception {
                logger.info("收到广播:");
                BroadcastState<String, Map<Long,AreaFenceInfo>> state =  ctx.getBroadcastState(CONFIG_DESCRIPTOR);
                state.put("area_fence_info", value);
                loadFinish = true;
            }
        }).print();
        env.execute("FlinkLoadMysqlSource");
    }
}
