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

import com.github.pulsar.eco.spring.starter.env.Schema;
import com.github.pulsar.eco.spring.starter.exception.PulsarProducerException;
import com.github.pulsar.eco.spring.starter.option.PulsarOptions;
import com.google.common.collect.Maps;
import com.google.protobuf.GeneratedMessageV3;
import java.util.Map;
import lombok.SneakyThrows;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.ProducerBuilder;
import org.apache.pulsar.client.api.PulsarClient;
import org.springframework.stereotype.Component;

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
    return PRODUCER_POOL.getOrDefault(options, createNew(options, createIfAbsent));
  }
}
