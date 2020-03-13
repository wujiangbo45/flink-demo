package com.navinfo.opentsp.flink.streaming.transformation;

import com.navinfo.opentsp.flink.pojo.TboxDataPojo;
import com.navinfo.opentsp.platform.location.protocol.common.LCLocationData;
import com.twitter.chill.protobuf.ProtobufSerializer;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.QueryableStateOptions;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.KafkaDeserializationSchema;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class KafkaSourceTest {

    /**
     * flink消费kafka数据
     */
    private static final Logger logger = LoggerFactory.getLogger(KafkaSourceTest.class);
    public static void main(String[] args) throws Exception {
        Configuration config = new Configuration();

        //启用Queryable State服务
        config.setBoolean(QueryableStateOptions.ENABLE_QUERYABLE_STATE_PROXY_SERVER, true);
        config.setInteger("rest.port",8333);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(config);

        // 自定义序列化器，如果proto数据需要在流中传递，需要自定义proto的序列化器
        env.getConfig().registerTypeWithKryoSerializer(LCLocationData.LocationData.class, ProtobufSerializer.class);

        // 为kafka提交offset设置检查点
        env.enableCheckpointing(5000);

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "10.30.50.214:21492");
        properties.setProperty("group.id", "flume-test");

        // proto数据在流中传递
        KafkaDeserializationSchema<LCLocationData.LocationData> deserializationSchemaForProto = new KafkaDeserializationSchema<LCLocationData.LocationData>() {

            @Override
            public TypeInformation<LCLocationData.LocationData> getProducedType() {
                return TypeInformation.of(LCLocationData.LocationData.class);
            }

            @Override
            public boolean isEndOfStream(LCLocationData.LocationData nextElement) {
                return false;
            }

            @Override
            public LCLocationData.LocationData deserialize(ConsumerRecord<byte[], byte[]> consumerRecord) throws Exception {
                LCLocationData.LocationData locationData = LCLocationData.LocationData.parseFrom(consumerRecord.value());
                logger.info("收到kafka数据：tid:{}", locationData.getTerminalId());
                return locationData;
            }


        };

        // 消费到proto数据之后直接转换成pojo并且在流中传递
        KafkaDeserializationSchema<TboxDataPojo> deserializationSchema = new KafkaDeserializationSchema<TboxDataPojo>() {

            @Override
            public TypeInformation<TboxDataPojo> getProducedType() {
                return TypeInformation.of(TboxDataPojo.class);
            }

            @Override
            public boolean isEndOfStream(TboxDataPojo nextElement) {
                return false;
            }

            @Override
            public TboxDataPojo deserialize(ConsumerRecord<byte[], byte[]> consumerRecord) throws Exception {
                LCLocationData.LocationData locationData = LCLocationData.LocationData.parseFrom(consumerRecord.value());
                logger.info("收到kafka数据：tid:{}", locationData.getTerminalId());
                return build(locationData);
            }

            private TboxDataPojo build(LCLocationData.LocationData locationData){
                TboxDataPojo tboxDataPojo = new TboxDataPojo();
                tboxDataPojo.settId(locationData.getTerminalId());
                tboxDataPojo.setGpsTime(locationData.getGpsDate());
                tboxDataPojo.setLat(locationData.getLatitude());
                tboxDataPojo.setLon(locationData.getLongitude());
                return tboxDataPojo;

            }

        };


        env.addSource(new FlinkKafkaConsumer<>("locationDataPB", deserializationSchemaForProto ,properties))
        .filter(value -> value.getTerminalId() != 1L).print();

        env.execute("kafka-consumer-test");

    }
}
