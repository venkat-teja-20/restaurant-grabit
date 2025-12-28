package com.grabit.config;

import com.grabit.exception.CustomException;
import jakarta.persistence.EntityNotFoundException;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, Map<String,String>> kafkaTemplate){
        DeadLetterPublishingRecoverer recoverer=new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record,ex)->new TopicPartition(
                        record.topic()+".DLT", record.partition()
                )
        );

        DefaultErrorHandler defaultErrorHandler=new DefaultErrorHandler(recoverer,new FixedBackOff(1000L,3));

        // Ignore these Exceptions
        defaultErrorHandler.addNotRetryableExceptions(
                EntityNotFoundException.class,
                CustomException.class
        );

        return defaultErrorHandler;
    }
}
