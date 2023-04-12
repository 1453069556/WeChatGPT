package com.gpt.chatproject.config;

import com.google.common.collect.Maps;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class RabbitMqConfig {

    @Value("${midjourney.queue.command.max_thread}")
    private Integer MAX_THREAD;
    @Value("${midjourney.queue.command.max_command_length}")
    private Integer MAX_COMMAND_LENGTH;

    @Bean
    public Queue midjourneyCommandQueue(@Value("${midjourney.queue.command.name}") String queueName) {
        final boolean durable = false;
        final boolean exclusive = false;
        final boolean autoDelete = false;
        Map<String, Object> params = Maps.newHashMap();
        /** x-message-ttl: 消息存活时间。单位为毫秒，即消息在队列中存在的最大时间。如果消息在此时间内没有被消费者消费，则会被队列自动删除。
         * x-expires: 队列闲置时间。单位为毫秒，即队列在没有任何消费者连接时的最大空闲时间。如果队列在此时间内没有被使用，则会被自动删除。
         * x-max-length: 队列最大长度，存放多少条消息。如果队列中的消息数量超过了这个限制，则会根据特定的策略进行消息的丢弃或移除。
         * x-max-length-bytes: 队列最大占用多少字节。如果队列中所有消息的总大小超过了这个限制，则会根据特定的策略进行消息的丢弃或移除。
         * x-dead-letter-exchange: 消息移出到哪个交换机。如果队列中的消息无法被消费者处理，则可以将它们移动到另一个交换机，以便稍后再次尝试处理。
         * x-dead-letter-routing-key: 交换机的路由键是什么。如果消息被移动到另一个交换机，则可以指定该交换机使用的路由键。
         * x-max-priority: 最大优先级是多少。在队列中可以定义一组有限数量的优先级，这个参数可以限制最大优先级的数量。
         * x-queue-mode: 延迟模式。可以定义两种队列模式： "default" 和 "lazy"，默认为 "default" 模式。 "lazy" 模式可以减少队列初始化时的内存消耗，
         * 但是在使用时可能会有一定的延迟。
         */
        params.put("x-max-length", MAX_COMMAND_LENGTH);
        return new Queue(queueName, durable, exclusive, autoDelete, params);
    }

    @Bean
    public Queue midjourneyResultQueue(@Value("${midjourney.queue.result.name}") String queueName) {
        final boolean durable = false;
        final boolean exclusive = false;
        final boolean autoDelete = false;
        Map<String, Object> params = Maps.newHashMap();
        return new Queue(queueName, durable, exclusive, autoDelete, params);
    }

    /**
     * 指定最大消费线程
     *
     * @param connectionFactory connectionFactory
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMaxConcurrentConsumers(MAX_THREAD); // 最多3个线程同时消费
        return factory;
    }

}
