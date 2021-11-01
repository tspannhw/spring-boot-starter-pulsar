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

package com.github.pulsar.eco.spring.starter.option;

import com.github.pulsar.eco.spring.starter.env.Schema;
import lombok.Builder;
import lombok.Data;
import org.apache.pulsar.client.api.CompressionType;
import org.apache.pulsar.client.api.HashingScheme;
import org.apache.pulsar.client.api.MessageRoutingMode;
import org.apache.pulsar.client.api.ProducerCryptoFailureAction;

@Data
@Builder
public class PulsarOptions {
  private Schema schema;

  private String topics;
  // Producer name
  private String producerName;
  /**
   * Message send timeout in ms.
   *
   * <p>If a message is not acknowledged by a server before the sendTimeout expires, an error
   * occurs.
   */
  private Long sendTimeoutMs;

  /**
   * If it is set to true, when the outgoing message queue is full, the Send and SendAsync methods
   * of producer block, rather than failing and throwing errors.
   *
   * <p>If it is set to false, when the outgoing message queue is full, the Send and SendAsync
   * methods of producer fail and ProducerQueueIsFullError exceptions occur.
   *
   * <p>The MaxPendingMessages parameter determines the size of the outgoing message queue.
   */
  private Boolean blockIfQueueFull;

  /**
   * The maximum size of a queue holding pending messages.
   *
   * <p>For example, a message waiting to receive an acknowledgment from a broker.
   *
   * <p>By default, when the queue is full, all calls to the Send and SendAsync methods fail unless
   * you set BlockIfQueueFull to true.
   */
  private Integer maxPendingMessages;

  /**
   * The maximum number of pending messages across partitions.
   *
   * <p>Use the setting to lower the max pending messages for each partition ({@link
   * #setMaxPendingMessages(int)}) if the total number exceeds the configured value.
   */
  private Integer maxPendingMessagesAcrossPartitions;

  /**
   * Message routing logic for producers on partitioned topics.
   *
   * <p>Apply the logic only when setting no key on messages.
   *
   * <p>Available options are as follows:
   *
   * <p>pulsar.RoundRobinDistribution: round robin
   *
   * <p>pulsar.UseSinglePartition: publish all messages to a single partition
   *
   * <p>pulsar.CustomPartition: a custom partitioning scheme
   */
  private MessageRoutingMode messageRoutingMode;

  /**
   * Hashing function determining the partition where you publish a particular message (partitioned
   * topics only).
   *
   * <p>Available options are as follows:
   *
   * <p>pulsar.JavaStringHash: the equivalent of String.hashCode() in Java
   *
   * <p>pulsar.Murmur3_32Hash: applies the Murmur3 hashing function
   *
   * <p>pulsar.BoostHash: applies the hashing function from C++'s Boost library
   */
  private HashingScheme hashingScheme;

  /**
   * Producer should take action when encryption fails.
   *
   * <p>FAIL: if encryption fails, unencrypted messages fail to send.
   *
   * <p>SEND: if encryption fails, unencrypted messages are sent.
   */
  private ProducerCryptoFailureAction cryptoFailureAction;

  /** Batching time period of sending messages. */
  private Long batchingMaxPublishDelayMicros;
  //	The maximum number of messages permitted in a batch.
  private Integer batchingMaxMessages;
  // Enable batching of messages.
  private Boolean batchingEnabled;
  /**
   * Message data compression type used by a producer.
   *
   * <p>Available options: LZ4 ZLIB ZSTD SNAPPY
   */
  private CompressionType compressionType;
}
