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

package com.github.pulsar.eco.spring.starter.convertor;

import com.github.pulsar.eco.spring.starter.modal.Headers;
import org.apache.pulsar.client.api.Message;

/**
 * Pulsar message pojo convert to headers.
 *
 * @author mattisonchao
 * @since 2.8.1
 */
public class PulsarMessage2HeadersConvertor {

  public static Headers convert2(Message<?> message) {
    return Headers.builder()
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
  }
}
