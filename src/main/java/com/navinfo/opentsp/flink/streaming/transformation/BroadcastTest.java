package com.navinfo.opentsp.flink.streaming.transformation;

import com.navinfo.opentsp.flink.pojo.AreaFenceInfo;
import com.navinfo.opentsp.flink.streaming.source.Flink2MysqlSource;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.state.BroadcastState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.runtime.state.HeapBroadcastState;
import org.apache.flink.streaming.api.datastream.BroadcastConnectedStream;
import org.apache.flink.streaming.api.datastream.BroadcastStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.functions.co.BroadcastProcessFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.stream.Collectors;

public class BroadcastTest {
    private static final Logger logger = LoggerFactory.getLogger(BroadcastTest.class);

    // 加载mysql数据,并且将数据广播出去,固定时间加载一次
    public static void main(String[] args) throws Exception {
        final MapStateDescriptor<String, Map<Long,AreaFenceInfo>> CONFIG_DESCRIPTOR = new MapStateDescriptor<>(
                "broadcast_cache",
                BasicTypeInfo.STRING_TYPE_INFO,
                Types.MAP(Types.LONG, Types.POJO(AreaFenceInfo.class)));
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        BroadcastStream<Map<Long, AreaFenceInfo>> broadcast = env.addSource(new Flink2MysqlSource())
                .setParallelism(1).map(value -> value.stream().collect(Collectors.toMap(AreaFenceInfo::getAreaId, areaFenceInfo1 -> areaFenceInfo1))).
                        returns(Types.MAP(Types.LONG,Types.POJO(AreaFenceInfo.class)))
                        .broadcast(CONFIG_DESCRIPTOR);
        DataStreamSource<String> integerDataStreamSource = env.socketTextStream("localhost", 9999);
        // 数据流与广播流连接
        BroadcastConnectedStream<String, Map<Long, AreaFenceInfo>> connect = integerDataStreamSource.connect(broadcast);
        connect.process(new BroadcastProcessFunction<String, Map<Long, AreaFenceInfo>, Object>() {
            @Override
            public void processElement(String value, ReadOnlyContext ctx, Collector<Object> out) {
                HeapBroadcastState<String,Map<Long,AreaFenceInfo>> config = (HeapBroadcastState<String,Map<Long,AreaFenceInfo>>)ctx.getBroadcastState(CONFIG_DESCRIPTOR);
                if(config.contains("area_fence_info")){
                    if (config.get("area_fence_info").get(Long.parseLong(value)) != null)
                        out.collect(value);
                };

            }
            @Override
            public void processBroadcastElement(Map<Long, AreaFenceInfo> value, Context ctx, Collector<Object> out) throws Exception {
                logger.info("一级处理类收到广播");
                BroadcastState<String, Map<Long,AreaFenceInfo>> state =  ctx.getBroadcastState(CONFIG_DESCRIPTOR);
                state.put("area_fence_info", value);

            }

        }).print();

        connect.process(new BroadcastProcessFunction<String, Map<Long, AreaFenceInfo>, Object>() {
            @Override
            public void processElement(String value, ReadOnlyContext ctx, Collector<Object> out) {
                HeapBroadcastState<String,Map<Long,AreaFenceInfo>> config = (HeapBroadcastState<String,Map<Long,AreaFenceInfo>>)ctx.getBroadcastState(CONFIG_DESCRIPTOR);
                if(config.contains("area_fence_info")){
                    if (config.get("area_fence_info").get(Long.parseLong(value)) != null)
                        out.collect(value);
                };

            }
            @Override
            public void processBroadcastElement(Map<Long, AreaFenceInfo> value, Context ctx, Collector<Object> out) throws Exception {
                logger.info("二级处理类收到广播");
                BroadcastState<String, Map<Long,AreaFenceInfo>> state =  ctx.getBroadcastState(CONFIG_DESCRIPTOR);
                state.put("area_fence_info", value);

            }

        }).print();

        env.execute("FlinkLoadMysqlSource");
    }
}
