/*
 *     Licensed to the Apache Software Foundation (ASF) under one
 *     or more contributor license agreements.  See the NOTICE file
 *     distributed with this work for additional information
 *     regarding copyright ownership.  The ASF licenses this file
 *     to you under the Apache License, Version 2.0 (the
 *     "License"); you may not use this file except in compliance
 *     with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing,
 *     software distributed under the License is distributed on an
 *     "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *     KIND, either express or implied.  See the License for the
 *     specific language governing permissions and limitations
 *     under the License.
 */

package com.github.pulsar.eco.spring.starter.delegate;

import com.github.pulsar.eco.spring.starter.annotation.PulsarListener;
import com.github.pulsar.eco.spring.starter.annotation.PulsarPayload;
import com.github.pulsar.eco.spring.starter.annotation.PulsarProperties;
import com.github.pulsar.eco.spring.starter.constant.Symbol;
import com.github.pulsar.eco.spring.starter.env.Schema;
import com.github.pulsar.eco.spring.starter.exception.PulsarClientConfigException;
import com.github.pulsar.eco.spring.starter.exception.PulsarConsumerException;
import com.github.pulsar.eco.spring.starter.exception.PulsarIllegalStateException;
import com.github.pulsar.eco.spring.starter.modal.Headers;
import com.github.pulsar.eco.spring.starter.scanner.ConsumerScanner;
import com.google.protobuf.GeneratedMessageV3;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.ConsumerBuilder;
import org.apache.pulsar.client.api.DeadLetterPolicy;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Slf4j
@DependsOn({"pulsarClient", "consumerScanner"})
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class ConsumerDelegate {
  private final ConsumerScanner scanner;
  private final PulsarClient pulsarClient;

  public ConsumerDelegate(ConsumerScanner scanner, PulsarClient pulsarClient) {
    this.scanner = scanner;
    this.pulsarClient = pulsarClient;
  }

  @SuppressWarnings("unchecked")
  private ConsumerBuilder<?> getConsumerBySchema(Class<?> pojo, Schema schema) {
    switch (schema) {
      case PROTOBUF:
        Class<? extends GeneratedMessageV3> protoBufPojo =
            (Class<? extends GeneratedMessageV3>) pojo;
        return pulsarClient.newConsumer(org.apache.pulsar.client.api.Schema.PROTOBUF(protoBufPojo));
      case AVRO:
        return pulsarClient.newConsumer(org.apache.pulsar.client.api.Schema.AVRO(pojo));
      case JSON:
        return pulsarClient.newConsumer(org.apache.pulsar.client.api.Schema.JSON(pojo));
      case STRING:
        return pulsarClient.newConsumer(org.apache.pulsar.client.api.Schema.STRING);
      case BYTES:
        return pulsarClient.newConsumer();
    }
    throw new PulsarIllegalStateException(
        String.format("Fail to instant consumer, because of wrong schema %s", schema));
  }

  @EventListener(ApplicationReadyEvent.class)
  public void init() {
    log.info(
        "{} already load {} pulsar consumers", Symbol.PREFIX_EMOJI, scanner.getContainer().size());
    scanner
        .getContainer()
        .forEach(
            (consumerUniqueKey, consumer) -> {
              Method handler = consumer.getHandler();
              PulsarListener listener = consumer.getListener();
              Schema schema = listener.schema();
              List<Parameter> methodTypes =
                  Arrays.stream(handler.getParameters())
                      .filter(param -> param.isAnnotationPresent(PulsarPayload.class))
                      .collect(Collectors.toList());
              if (CollectionUtils.isEmpty(methodTypes)
                  && schema != Schema.BYTES
                  && schema != Schema.STRING) {
                throw new PulsarClientConfigException(
                    String.format(
                        "Do not find payload in %s handler, maybe you forget to annotate it?",
                        handler.getName()));
              }
              if (methodTypes.size() > 1) {
                throw new PulsarClientConfigException(
                    String.format(
                        "Find more than 1 payload in handler, the handler name is %s",
                        handler.getName()));
              }
              Class<?> payloadKlass = methodTypes.get(0).getType();
              ConsumerBuilder<?> consumerBySchema = getConsumerBySchema(payloadKlass, schema);
              consumerBySchema.topic(listener.topicNames());
              if (StringUtils.isNotBlank(listener.topicsPattern())) {
                consumerBySchema.topicsPattern(listener.topicsPattern());
              }
              try {
                consumerBySchema
                    .subscriptionName(listener.subscriptionName())
                    .subscriptionType(listener.subscriptionType())
                    .receiverQueueSize(listener.receiverQueueSize())
                    .acknowledgmentGroupTime(
                        listener.acknowledgementsGroupTimeMicros(), TimeUnit.MICROSECONDS)
                    .negativeAckRedeliveryDelay(
                        listener.negativeAckRedeliveryDelayMicros(), TimeUnit.MICROSECONDS)
                    .maxTotalReceiverQueueSizeAcrossPartitions(
                        listener.maxTotalReceiverQueueSizeAcrossPartitions())
                    .consumerName(listener.consumerName())
                    .ackTimeout(listener.ackTimeoutMillis(), TimeUnit.MILLISECONDS)
                    .ackTimeoutTickTime(listener.tickDurationMillis(), TimeUnit.MILLISECONDS)
                    .priorityLevel(listener.priorityLevel())
                    .cryptoFailureAction(listener.cryptoFailureAction());
                Map<String, String> properties =
                    Arrays.stream(listener.properties())
                        .collect(Collectors.toMap(PulsarProperties::key, PulsarProperties::value));
                if (!CollectionUtils.isEmpty(properties)) {
                  consumerBySchema.properties(properties);
                }
                consumerBySchema
                    .readCompacted(listener.readCompacted())
                    .subscriptionInitialPosition(listener.subscriptionInitialPosition())
                    .patternAutoDiscoveryPeriod(listener.patternAutoDiscoveryPeriod())
                    .subscriptionTopicsMode(listener.regexSubscriptionMode());
                if (listener.deadLetterPolicy().maxRedeliverCount() != 0) {
                  consumerBySchema.deadLetterPolicy(
                      DeadLetterPolicy.builder()
                          .maxRedeliverCount(listener.deadLetterPolicy().maxRedeliverCount())
                          .deadLetterTopic(listener.deadLetterPolicy().deadLetterTopic())
                          .retryLetterTopic(listener.deadLetterPolicy().retryLetterTopic())
                          .build());
                }
                Consumer<?> subscriber =
                    consumerBySchema
                        .autoUpdatePartitions(listener.autoUpdatePartitions())
                        .replicateSubscriptionState(listener.replicateSubscriptionState())
                        .subscribe();
                CompletableFuture<? extends Message<?>> asyncReceiveFuture =
                    subscriber.receiveAsync();
                asyncReceiveFuture.whenCompleteAsync(
                    (message, error) -> {
                      if (error != null) {
                        throw new PulsarConsumerException("Got new client exception.", error);
                      }
                      Headers headers =
                          Headers.builder()
                              .isReplicated(message.isReplicated())
                              .eventTime(message.getEventTime())
                              .producerName(message.getProducerName())
                              .key(message.getKey())
                              .publishTime(message.getPublishTime())
                              .redeliveryCount(message.getRedeliveryCount())
                              .replicatedFrom(message.getReplicatedFrom())
                              .schemaVersion(message.getSchemaVersion())
                              .sequenceId(message.getSequenceId())
                              .topicName(message.getTopicName())
                              .properties(message.getProperties())
                              .build();
                      Object payload = message.getValue();
                      Method delegateHandler = consumer.getHandler();
                      Parameter[] parameters = delegateHandler.getParameters();
                      Object[] params = new Object[parameters.length];
                      for (int i = 0; i < parameters.length; i++) {
                        if (parameters[i].isAnnotationPresent(PulsarPayload.class)) {
                          params[i] = payload;
                          continue;
                        }
                        if (parameters[i].getType() == Headers.class) {
                          params[i] = headers;
                          continue;
                        }
                        params[i] = null;
                      }
                      try {
                        consumer.getHandler().setAccessible(true);
                        consumer.getHandler().invoke(consumer.getDelegator(), params);
                        subscriber.acknowledge(message);
                      } catch (IllegalAccessException
                          | InvocationTargetException
                          | PulsarClientException e) {
                        subscriber.negativeAcknowledge(message);
                        e.printStackTrace();
                      }
                    });
              } catch (PulsarClientException e) {
                throw new PulsarConsumerException(
                    String.format("Fail to create consumer %s", listener.consumerName()), e);
              }
            });
  }
}
