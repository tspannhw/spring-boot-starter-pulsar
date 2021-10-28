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

package com.github.pulsar.eco.spring.starter.annotation;

import com.github.pulsar.eco.spring.starter.env.Schema;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.apache.pulsar.client.api.ConsumerCryptoFailureAction;
import org.apache.pulsar.client.api.RegexSubscriptionMode;
import org.apache.pulsar.client.api.SubscriptionInitialPosition;
import org.apache.pulsar.client.api.SubscriptionType;

@Target({ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PulsarListener {
  // Topic name
  String[] topicNames();
  // Schema
  Schema schema() default Schema.BYTES;
  // Topic pattern
  String topicsPattern() default "";
  // Subscription name
  String subscriptionName() ;

  /**
   * Subscription type
   *
   * <p>Four subscription types are available: Exclusive Failover Shared Key_Shared
   */
  SubscriptionType subscriptionType() default SubscriptionType.Exclusive;

  /**
   * Size of a consumer's receiver queue.
   *
   * <p>For example, the number of messages accumulated by a consumer before an application calls
   * Receive.
   *
   * <p>A value higher than the default value increases consumer throughput, though at the expense
   * of more memory utilization.
   */
  int receiverQueueSize() default 1000;

  /**
   * Group a consumer acknowledgment for a specified time.
   *
   * <p>By default, a consumer uses 100ms grouping time to send out acknowledgments to a broker.
   *
   * <p>Setting a group time of 0 sends out acknowledgments immediately.
   *
   * <p>A longer ack group time is more efficient at the expense of a slight increase in message
   * re-deliveries after a failure.
   */
  long acknowledgementsGroupTimeMicros() default 100000;

  /**
   * Delay to wait before redelivering messages that failed to be processed.
   *
   * <p>When an application uses {@link Consumer#negativeAcknowledge(Message)}, failed messages are
   * redelivered after a fixed timeout.
   */
  long negativeAckRedeliveryDelayMicros() default 60000000;

  /**
   * The max total receiver queue size across partitions.
   *
   * <p>This setting reduces the receiver queue size for individual partitions if the total receiver
   * queue size exceeds this value.
   */
  int maxTotalReceiverQueueSizeAcrossPartitions() default 50000;
  // Consumer name
  String consumerName();
  // Timeout of unacked messages
  long ackTimeoutMillis() default 0;

  /**
   * Granularity of the ack-timeout redelivery.
   *
   * <p>Using an higher tickDurationMillis reduces the memory overhead to track messages when
   * setting ack-timeout to a bigger value (for example, 1 hour).
   */
  long tickDurationMillis() default 1000;

  /**
   * Priority level for a consumer to which a broker gives more priority while dispatching messages
   * in the shared subscription mode.
   *
   * <p>The broker follows descending priorities. For example, 0=max-priority, 1, 2,...
   *
   * <p>In shared subscription mode, the broker first dispatches messages to the max priority level
   * consumers if they have permits. Otherwise, the broker considers next priority level consumers.
   *
   * <p>Example 1
   *
   * <p>If a subscription has consumerA with priorityLevel 0 and consumerB with priorityLevel 1,
   * then the broker only dispatches messages to consumerA until it runs out permits and then starts
   * dispatching messages to consumerB.
   *
   * <p>Example 2
   *
   * <p>Consumer Priority, Level, Permits C1, 0, 2 C2, 0, 1 C3, 0, 1 C4, 1, 2 C5, 1, 1
   *
   * <p>Order in which a broker dispatches messages to consumers is: C1, C2, C3, C1, C4, C5, C4.
   */
  int priorityLevel() default 0;

  /**
   * Consumer should take action when it receives a message that can not be decrypted.
   *
   * <p>FAIL: this is the default option to fail messages until crypto succeeds.
   *
   * <p>DISCARD:silently acknowledge and not deliver message to an application.
   *
   * <p>CONSUME: deliver encrypted messages to applications. It is the application's responsibility
   * to decrypt the message.
   *
   * <p>The decompression of message fails.
   *
   * <p>If messages contain batch messages, a client is not be able to retrieve individual messages
   * in batch.
   *
   * <p>Delivered encrypted message contains {@link EncryptionContext} which contains encryption and
   * compression information in it using which application can decrypt consumed message payload.
   */
  ConsumerCryptoFailureAction cryptoFailureAction() default ConsumerCryptoFailureAction.FAIL;

  /**
   * A name or value property of this consumer.
   *
   * <p>properties is application defined metadata attached to a consumer.
   *
   * <p>When getting a topic stats, associate this metadata with the consumer stats for easier
   * identification.
   */
  PulsarProperties[] properties() default {};

  /**
   * If enabling readCompacted, a consumer reads messages from a compacted topic rather than reading
   * a full message backlog of a topic.
   *
   * <p>A consumer only sees the latest value for each key in the compacted topic, up until reaching
   * the point in the topic message when compacting backlog. Beyond that point, send messages as
   * normal.
   *
   * <p>Only enabling readCompacted on subscriptions to persistent topics, which have a single
   * active consumer (like failure or exclusive subscriptions).
   *
   * <p>Attempting to enable it on subscriptions to non-persistent topics or on shared subscriptions
   * leads to a subscription call throwing a PulsarClientException.
   */
  boolean readCompacted() default false;

  /** Initial position at which to set cursor when subscribing to a topic at first time. */
  SubscriptionInitialPosition subscriptionInitialPosition() default
      SubscriptionInitialPosition.Latest;

  /**
   * Topic auto discovery period when using a pattern for topic's consumer.
   *
   * <p>The default and minimum value is 1 minute.
   */
  int patternAutoDiscoveryPeriod() default 1;

  /**
   * When subscribing to a topic using a regular expression, you can pick a certain type of topics.
   *
   * <p>PersistentOnly: only subscribe to persistent topics.
   *
   * <p>NonPersistentOnly: only subscribe to non-persistent topics.
   *
   * <p>AllTopics: subscribe to both persistent and non-persistent topics.
   */
  RegexSubscriptionMode regexSubscriptionMode() default RegexSubscriptionMode.PersistentOnly;

  /**
   * Dead letter policy for consumers.
   *
   * <p>By default, some messages are probably redelivered many times, even to the extent that it
   * never stops.
   *
   * <p>By using the dead letter mechanism, messages have the max redelivery count. When exceeding
   * the maximum number of redeliveries, messages are sent to the Dead Letter Topic and acknowledged
   * automatically.
   *
   * <p>You can enable the dead letter mechanism by setting deadLetterPolicy.
   *
   * <p>Example
   *
   * <p>client.newConsumer()
   * .deadLetterPolicy(DeadLetterPolicy.builder().maxRedeliverCount(10).build()) .subscribe();
   *
   * <p>Default dead letter topic name is {TopicName}-{Subscription}-DLQ.
   *
   * <p>To set a custom dead letter topic name: client.newConsumer()
   * .deadLetterPolicy(DeadLetterPolicy.builder().maxRedeliverCount(10)
   * .deadLetterTopic("your-topic-name").build()) .subscribe();
   *
   * <p>When specifying the dead letter policy while not specifying ackTimeoutMillis, you can set
   * the ack timeout to 30000 millisecond.
   */
  PulsarDeadLetterPolicy deadLetterPolicy() default
      @PulsarDeadLetterPolicy(maxRedeliverCount = 0, retryLetterTopic = "", deadLetterTopic = "");

  /**
   * If autoUpdatePartitions is enabled, a consumer subscribes to partition increasement
   * automatically.
   *
   * <p>Note: this is only for partitioned consumers.
   */
  boolean autoUpdatePartitions() default true;

  /**
   * If replicateSubscriptionState is enabled, a subscription state is replicated to geo-replicated
   * clusters.
   */
  boolean replicateSubscriptionState() default false;
}
