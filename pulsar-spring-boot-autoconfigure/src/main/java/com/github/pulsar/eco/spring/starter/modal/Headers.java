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

package com.github.pulsar.eco.spring.starter.modal;

import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Headers {
  /** Messages are optionally tagged with keys, which is useful for things like topic compaction. */
  private String key;
  /** An optional key/value map of user-defined properties.* */
  private Map<String, String> properties;
  /**
   * The name of the producer who produces the message. If you do not specify a producer name, the
   * default name is used.
   */
  private String producerName;

  /**
   * Each Pulsar message belongs to an ordered sequence on its topic. The sequence ID of the message
   * is its order in that sequence.
   */
  private Long sequenceId;

  /**
   * The timestamp of when the message is published. The timestamp is automatically applied by the
   * producer.
   */
  private Long publishTime;
  /**
   * An optional timestamp attached to a message by applications. For example, applications attach a
   * timestamp on when the message is processed. If nothing is set to event time, the value is 0.
   */
  private Long eventTime;

  /** The topic the message was published to */
  private String topicName;

  /**
   * Message redelivery count, redelivery count maintain in pulsar broker. When client acknowledge
   * message timeout, broker will dispatch message again with message redelivery count in
   * CommandMessage defined.
   *
   * <p>Message redelivery increases monotonically in a broker, when topic switch ownership to a
   * another broker redelivery count will be recalculated.
   */
  private int redeliveryCount;

  /** Schema version of the message. */
  private byte[] schemaVersion;

  /** Whether the message is replicated from other cluster. */
  private boolean isReplicated;

  /** The name of cluster, from which the message is replicated. */
  private String replicatedFrom;

}
