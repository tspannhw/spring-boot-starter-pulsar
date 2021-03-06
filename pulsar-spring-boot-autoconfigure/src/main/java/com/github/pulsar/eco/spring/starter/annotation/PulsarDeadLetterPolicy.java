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

import org.apache.pulsar.client.api.ConsumerBuilder;

/**
 * Configuration for the "dead letter queue" feature in consumer.
 *
 * @see ConsumerBuilder#deadLetterPolicy(org.apache.pulsar.client.api.DeadLetterPolicy)
 * @since 1.0.0
 * @author mattison
 */
public @interface PulsarDeadLetterPolicy {
  /**
   * Maximum number of times that a message will be redelivered before being sent to the dead letter
   * queue.
   */
  int maxRedeliverCount();

  /** Name of the retry topic where the failing messages will be sent. */
  String retryLetterTopic();

  /** Name of the dead topic where the failing messages will be sent. */
  String deadLetterTopic();
}
