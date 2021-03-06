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

package com.github.pulsar.eco.spring.starter.pool;

import static com.github.pulsar.eco.spring.starter.utils.CommonUtils.*;
import com.github.pulsar.eco.spring.starter.env.Schema;
import com.github.pulsar.eco.spring.starter.exception.PulsarProducerException;
import com.github.pulsar.eco.spring.starter.option.PulsarOptions;
import com.google.common.collect.Maps;
import com.google.protobuf.GeneratedMessageV3;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.ProducerBuilder;
import org.apache.pulsar.client.api.PulsarClient;
import org.springframework.stereotype.Component;

/**
 * Create pool {@link java.util.concurrent.ConcurrentHashMap} to save all producer. Support get and
 * create new producer method.
 *
 * @author mattison
 * @since 1.0.0
 */
@Component
public class ProducerPool {
  private final PulsarClient pulsarClient;
  private static final Map<PulsarOptions, Producer<?>> PRODUCER_POOL = Maps.newConcurrentMap();

  public ProducerPool(PulsarClient pulsarClient) {
    this.pulsarClient = pulsarClient;
  }

  @SneakyThrows
  private Producer<?> createNew(PulsarOptions options, Object obj) {
    ProducerBuilder<?> builder = getProducerBuilderBySchema(options.getSchema(), obj);
    builder.topic(options.getTopics());
    ifPresentStrThen(options.getProducerName(), builder::producerName);
    ifPresentObjThen(
        options.getSendTimeoutMs(),
        (timeoutMs) -> builder.sendTimeout(timeoutMs, TimeUnit.MILLISECONDS));
    ifPresentObjThen(options.getBlockIfQueueFull(), builder::blockIfQueueFull);
    ifPresentObjThen(options.getMaxPendingMessages(), builder::maxPendingMessages);
    ifPresentObjThen(
        options.getMaxPendingMessagesAcrossPartitions(),
        builder::maxPendingMessagesAcrossPartitions);
    ifPresentObjThen(options.getMessageRoutingMode(), builder::messageRoutingMode);
    ifPresentObjThen(options.getHashingScheme(), builder::hashingScheme);
    ifPresentObjThen(options.getCryptoFailureAction(), builder::cryptoFailureAction);
    ifPresentObjThen(
        options.getBatchingMaxPublishDelayMicros(),
        (times) ->
            builder.batchingMaxPublishDelay(
                options.getBatchingMaxPublishDelayMicros(), TimeUnit.MICROSECONDS));
    ifPresentObjThen(options.getBatchingMaxMessages(), builder::batchingMaxMessages);
    ifPresentObjThen(options.getBatchingEnabled(), builder::enableBatching);
    ifPresentObjThen(options.getCompressionType(), builder::compressionType);
    return builder.create();
  }

  @SuppressWarnings("unchecked")
  private ProducerBuilder<?> getProducerBuilderBySchema(Schema schema, Object obj) {
    switch (schema) {
      case BYTES:
        return pulsarClient.newProducer();
      case STRING:
        return pulsarClient.newProducer(org.apache.pulsar.client.api.Schema.STRING);
      case JSON:
        return pulsarClient.newProducer(org.apache.pulsar.client.api.Schema.JSON(obj.getClass()));
      case AVRO:
        return pulsarClient.newProducer(org.apache.pulsar.client.api.Schema.AVRO(obj.getClass()));
      case PROTOBUF:
        Class<? extends GeneratedMessageV3> protoBufPojo =
            (Class<? extends GeneratedMessageV3>) obj.getClass();
        return pulsarClient.newProducer(org.apache.pulsar.client.api.Schema.PROTOBUF(protoBufPojo));
    }
    throw new PulsarProducerException(
        String.format("pulsar parse producer schema %s fail.", schema));
  }

  public Producer<?> getOrCreateIfAbsent(PulsarOptions options, Object createIfAbsent) {
    Producer<?> producer = PRODUCER_POOL.get(options);
    if (producer == null) {
      Producer<?> newProducer = createNew(options, createIfAbsent);
      PRODUCER_POOL.put(options, newProducer);
      return newProducer;
    }
    return producer;
  }
}
