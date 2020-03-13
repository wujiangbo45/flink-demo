package com.navinfo.opentsp.flink.streaming.transformation;

import com.alibaba.fastjson.JSON;
import com.navinfo.opentsp.flink.pojo.TboxDataPojo;
import com.navinfo.opentsp.flink.streaming.source.Flink2MysqlSource;
import javafx.scene.control.Alert;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.ConfigConstants;
import org.apache.flink.configuration.ConfigOptions;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.QueryableStateOptions;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;

public class TboxOnOffLine {

    private static final Logger logger = LoggerFactory.getLogger(TboxDataPojo.class);
    // 终端模块上下线
    // 某个终端连续上传5个点表示上线，一分钟不上传则设置终端下线
    public static void main(String[] args) throws Exception {
        logger.info("start");
        Configuration config = new Configuration();

        //启用Queryable State服务
        config.setBoolean(QueryableStateOptions.ENABLE_QUERYABLE_STATE_PROXY_SERVER, true);
        // flink web ui端口号，默认8081
        config.setInteger("rest.port",8333);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(config);
        DataStreamSource<String> stringDataStreamSource = env.socketTextStream("localhost", 9999);
        stringDataStreamSource.map(value -> JSON.parseObject(value, TboxDataPojo.class))
                .filter(Objects::nonNull)
                .keyBy(TboxDataPojo::gettId)
                .process(new KeyedProcessFunction<Long, TboxDataPojo, Tuple2<String, TboxDataPojo>>() {
                    private static final int MAX_SIZE_UPLOAD = 2;

                    private static final int MAX_TIME_OFFLINE = 60 * 1000;

                    private transient ValueState<Integer> uploadState;

                    private transient ValueState<TboxDataPojo> onlineState;

                    private transient ValueState<Long> timerState;


                    @Override
                    public void open(Configuration parameters) {
                        ValueStateDescriptor<Integer> uploadSizeDesc = new ValueStateDescriptor<>(
                                "uploadSize",
                                Types.INT
                        );
                        uploadState = getRuntimeContext().getState(uploadSizeDesc);

                        ValueStateDescriptor<TboxDataPojo> onlineStateDesc = new ValueStateDescriptor<>(
                                "onlineState",
                                Types.POJO(TboxDataPojo.class)
                        );

                        ValueStateDescriptor<Long> timerStateDesc = new ValueStateDescriptor<>(
                                "timerState",
                                Types.LONG);

                        // 设置状态值为可查询状态
                        onlineStateDesc.setQueryable("terminal-online");

                        timerState = getRuntimeContext().getState(timerStateDesc);
                        onlineState = getRuntimeContext().getState(onlineStateDesc);
                    }

                    @Override
                    public void processElement(TboxDataPojo value, Context ctx, Collector<Tuple2<String, TboxDataPojo>> out) throws Exception {
                        // 更新状态
                        uploadState.update(Optional.ofNullable(uploadState.value()).orElse(0) + 1);
                        if (uploadState.value() >= MAX_SIZE_UPLOAD && onlineState.value() == null){
                            onlineState.update(value);
                            out.collect(new Tuple2<>("终端" + value.gettId() + "上线！", value));
                        }

                        cleanUp(ctx);

                        if (onlineState.value() != null){
                            long timer = ctx.timerService().currentProcessingTime() + MAX_TIME_OFFLINE;
                            timerState.update(timer);
                            ctx.timerService().registerProcessingTimeTimer(timer);
                        }
                    }

                    @Override
                    public void onTimer(long timestamp, OnTimerContext ctx, Collector<Tuple2<String, TboxDataPojo>> out) throws Exception {
                        out.collect(new Tuple2<>("终端" + onlineState.value().gettId() + "下线！", onlineState.value()));
                        uploadState.clear();
                        onlineState.clear();
                    }

                    private void cleanUp(Context ctx) throws Exception {
                        // delete timer
                        Long timer = timerState.value();
                        if (timer != null){
                            ctx.timerService().deleteProcessingTimeTimer(timer);
                            // clean up all state
                            timerState.clear();
                        }

                    }
                }).map(value -> value.f0).print();
        env.execute("onOffline");

    }
}
