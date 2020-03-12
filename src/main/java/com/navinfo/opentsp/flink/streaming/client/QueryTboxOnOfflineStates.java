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

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.apache.flink.api.common.typeinfo.BasicTypeInfo.*;

public class QueryTboxOnOfflineStates {



    public static void main(String[] args) throws InterruptedException, IOException, ExecutionException {
        String jobid = "d47fbbaf8b504733d024ac86edae1bfd";

        QueryableStateClient client = new QueryableStateClient("localhost", 8081);
        ExecutionConfig config = new ExecutionConfig();
        config.setMaxParallelism(1);
        client.setExecutionConfig(config);

        ValueStateDescriptor<TboxDataPojo> onlineStateDesc = new ValueStateDescriptor<>(
                "onlineState",
                Types.POJO(TboxDataPojo.class)
        );
        Long key = 73443389L;
        JobID jobID = new JobID();

        CompletableFuture<ValueState<TboxDataPojo>> kvState;
        kvState = client.getKvState(JobID.fromHexString(jobid), "terminal-online", key, LONG_TYPE_INFO, onlineStateDesc);
        kvState.get();
        kvState.thenAccept(tboxDataPojoValueState -> {
            System.out.println(tboxDataPojoValueState);
        });
        Thread.sleep(5000L);
    }
}
