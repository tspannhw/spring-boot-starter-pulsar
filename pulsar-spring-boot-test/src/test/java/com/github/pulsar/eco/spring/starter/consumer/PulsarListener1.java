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

package com.github.pulsar.eco.spring.starter.consumer;

import com.github.pulsar.eco.spring.starter.annotation.PulsarHandler;
import com.github.pulsar.eco.spring.starter.annotation.PulsarListener;
import com.github.pulsar.eco.spring.starter.annotation.PulsarPayload;
import com.github.pulsar.eco.spring.starter.storage.InMemoryStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@PulsarListener(
    topicNames = "test-1",
    consumerName = "consumer-test-1",
    subscriptionName = "subscription-test-1")
@Service
public class PulsarListener1 {

  @PulsarHandler
  public void testConsumer(@PulsarPayload byte[] bytes) {
    InMemoryStore.cache.put("test-1", bytes);
  }
}
