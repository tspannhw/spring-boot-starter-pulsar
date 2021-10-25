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
import com.github.pulsar.eco.spring.starter.env.Schema;
import com.github.pulsar.eco.spring.starter.exception.PulsarClientConfigException;
import com.github.pulsar.eco.spring.starter.exception.PulsarIllegalStateException;
import com.github.pulsar.eco.spring.starter.scanner.ConsumerScanner;
import com.google.protobuf.GeneratedMessageV3;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.ConsumerBuilder;
import org.apache.pulsar.client.api.PulsarClient;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
@Slf4j
@DependsOn({"pulsarClient", "ConsumerScanner"})
public class ConsumerDelegate implements ApplicationListener<ApplicationReadyEvent> {
  private final ConsumerScanner scanner;
  private final PulsarClient pulsarClient;

  public ConsumerDelegate(ConsumerScanner scanner, PulsarClient pulsarClient) {
    this.scanner = scanner;
    this.pulsarClient = pulsarClient;
  }

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    scanner
        .getContainer()
        .forEach(
            (consumerUniqueKey, consumer) -> {
              Method handler = consumer.getHandler();
              PulsarListener listener = consumer.getListener();
              Schema schema = listener.schema();
              List<AnnotatedType> methodTypes =
                  Arrays.stream(handler.getAnnotatedParameterTypes())
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
              Class<? extends Type> payloadKlass = methodTypes.get(0).getType().getClass();
              ConsumerBuilder<?> consumerBySchema = getConsumerBySchema(payloadKlass, schema);

              // todo config consumer and consume message to delegate.
            });
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
}
