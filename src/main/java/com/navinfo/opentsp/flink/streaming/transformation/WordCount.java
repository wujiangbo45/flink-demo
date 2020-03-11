package com.navinfo.opentsp.flink.streaming.transformation;

import akka.stream.impl.CollectorState;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

public class WordCount {

    // use nc -lk 9999 to start tcp server

    /**
     * word count
     * 单词统计
     * 对输入的单词计数(统计每5秒内输入的单词数)
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStream<Tuple2<String, Integer>> dataStream = env
                .socketTextStream("localhost", 9999)
                .flatMap((String word, Collector<Tuple2<String,Integer>> out) -> {
                    for (String s : word.split(" ")) {
                        out.collect(new Tuple2<>(s, 1));
                    }
                })
                .returns(Types.TUPLE(Types.STRING,Types.INT))
                .keyBy(1)
                .timeWindow(Time.seconds(5))
                .sum(1).name("word count main ");
        dataStream.print();
        env.execute("Window WordCount");
    }


}
