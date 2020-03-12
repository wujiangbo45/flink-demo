package com.navinfo.opentsp.flink.streaming.client;

import com.navinfo.opentsp.flink.pojo.TboxDataPojo;
import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.ExecutionMode;
import org.apache.flink.api.common.JobID;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.queryablestate.client.QueryableStateClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.apache.flink.api.common.typeinfo.BasicTypeInfo.*;

public class QueryTboxOnOfflineStates {


    private static final Logger logger = LoggerFactory.getLogger(QueryTboxOnOfflineStates.class);

    public static void main(String[] args) throws InterruptedException, IOException, ExecutionException {
        String jobid = "9a34d9f71909bcd4269de837cd6b4088";
        // KvStateClientProxy端口号
        QueryableStateClient client = new QueryableStateClient("localhost", 9069);
        ExecutionConfig config = new ExecutionConfig();
        config.setMaxParallelism(1);
        client.setExecutionConfig(config);

        ValueStateDescriptor<TboxDataPojo> onlineStateDesc = new ValueStateDescriptor<>(
                "onlineState",
                Types.POJO(TboxDataPojo.class)
        );
        Long key = 73443389L;

        CompletableFuture<ValueState<TboxDataPojo>> kvState;
        kvState = client.getKvState(JobID.fromHexString(jobid), "terminal-online", key, LONG_TYPE_INFO, onlineStateDesc);
        kvState.thenAccept(response -> {
            try {
                logger.info("查询到数据:{}",response.value().toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        Thread.sleep(50000L);
    }
}
