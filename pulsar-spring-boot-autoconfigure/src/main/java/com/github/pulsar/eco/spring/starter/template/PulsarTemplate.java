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

package com.github.pulsar.eco.spring.starter.template;

import com.github.pulsar.eco.spring.starter.env.Schema;
import com.github.pulsar.eco.spring.starter.exception.PulsarProducerException;
import com.github.pulsar.eco.spring.starter.option.PulsarOptions;
import com.github.pulsar.eco.spring.starter.pool.ProducerPool;
import lombok.SneakyThrows;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.Producer;
import org.springframework.stereotype.Component;

@Component
public class PulsarTemplate {
  private final ProducerPool producerPool;

  public PulsarTemplate(ProducerPool producerPool) {
    this.producerPool = producerPool;
  }

  private MessageId send(byte[] bytes, String... topic) {
    PulsarOptions options =
        PulsarOptions.builder().schema(Schema.BYTES).topics(String.join(",", topic)).build();
    return send(bytes, options);
  }

  private MessageId send(String str, String... topic) {
    PulsarOptions options =
        PulsarOptions.builder().schema(Schema.STRING).topics(String.join(",", topic)).build();
    return send(str, options);
  }

  private MessageId sendJson(Object obj, String... topic) {
    PulsarOptions options =
        PulsarOptions.builder().schema(Schema.JSON).topics(String.join(",", topic)).build();
    return send(obj, options);
  }

  private MessageId sendAvro(Object obj, String... topic) {
    PulsarOptions options =
        PulsarOptions.builder().schema(Schema.AVRO).topics(String.join(",", topic)).build();
    return send(obj, options);
  }

  private MessageId sendProtobuf(Object obj, String... topic) {
    PulsarOptions options =
        PulsarOptions.builder().schema(Schema.PROTOBUF).topics(String.join(",", topic)).build();
    return send(obj, options);
  }

  @SneakyThrows
  @SuppressWarnings("unchecked")
  private MessageId send(Object obj, PulsarOptions options) {
    Producer<?> producer = producerPool.getOrCreateIfAbsent(options, obj);
    switch (options.getSchema()) {
      case STRING:
        return ((Producer<String>) producer).send((String) obj);
      case JSON:
      case AVRO:
      case PROTOBUF:
        return ((Producer<Object>) producer).send(obj);
      case BYTES:
        return ((Producer<byte[]>) producer).send((byte[]) obj);
    }
    throw new PulsarProducerException(
        String.format(
            "Wrong schema type, the producer is %s, the schema is %s",
            options.getProducerName(), options.getSchema()));
  }
}
