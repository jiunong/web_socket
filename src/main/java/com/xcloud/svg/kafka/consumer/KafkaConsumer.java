package com.xcloud.svg.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


/**
 * Hello!
 * Created By  JCccc on 2018/11/24
 * 13:13
 */
//@Component
public class KafkaConsumer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

//下面的主题是一个数组，可以同时订阅多主题，只需按数组格式即可，也就是用“，”隔开
    @KafkaListener(topics = {"ZT_DMS_JSSOMS"})
    public void receive(ConsumerRecord<?, ?> record){

        logger.info("消费得到的消息---key: " + record.key());
        logger.info("消费得到的消息---value: " + record.value().toString());
    }

}
