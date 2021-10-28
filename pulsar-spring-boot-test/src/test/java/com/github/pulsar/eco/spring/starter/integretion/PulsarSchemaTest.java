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

package com.github.pulsar.eco.spring.starter.integretion;

import com.github.pulsar.eco.spring.starter.Application;
import com.github.pulsar.eco.spring.starter.broker.BaseBroker;
import com.github.pulsar.eco.spring.starter.modal.Hero;
import com.github.pulsar.eco.spring.starter.proto.HeroWrapper;
import com.github.pulsar.eco.spring.starter.storage.InMemoryStore;
import java.nio.charset.StandardCharsets;
import javax.inject.Inject;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.ProducerBuilder;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest(classes = Application.class)
@ExtendWith(SpringExtension.class)
public class PulsarSchemaTest extends BaseBroker {

  @Inject private PulsarClient pulsarClient;

  @Test
  public void testBytes() throws PulsarClientException {
    ProducerBuilder<byte[]> producerBuilder = pulsarClient.newProducer();
    Producer<byte[]> producer =
        producerBuilder.topic("test-1").producerName("producer-test-1").create();
    byte[] message = "Hello Pulsar".getBytes(StandardCharsets.UTF_8);
    producer.send(message);
    Awaitility.await()
        .untilAsserted(() -> Assertions.assertNotNull(InMemoryStore.cache.get("test-1")));
    byte[] bytes = (byte[]) InMemoryStore.cache.get("test-1");
    String value = new String(bytes);
    Assertions.assertEquals(value, "Hello Pulsar");
  }

  @Test
  public void testString() throws PulsarClientException {
    ProducerBuilder<String> producerBuilder = pulsarClient.newProducer(Schema.STRING);
    Producer<String> producer =
        producerBuilder.topic("test-2").producerName("producer-test-2").create();
    producer.send("Hello Pulsar");
    Awaitility.await()
        .untilAsserted(() -> Assertions.assertNotNull(InMemoryStore.cache.get("test-2")));
    String value = (String) InMemoryStore.cache.get("test-2");
    Assertions.assertEquals(value, "Hello Pulsar");
  }

  @Test
  public void testJson() throws PulsarClientException {
    Hero hero = Hero.builder().name("Pulsar-Hero").age(28).duty("Save the world").build();
    ProducerBuilder<Hero> producerBuilder = pulsarClient.newProducer(Schema.JSON(Hero.class));
    Producer<Hero> producer =
        producerBuilder.topic("test-3").producerName("producer-test-3").create();
    producer.send(hero);
    Awaitility.await()
        .untilAsserted(() -> Assertions.assertNotNull(InMemoryStore.cache.get("test-3")));
    Hero value = (Hero) InMemoryStore.cache.get("test-3");
    Assertions.assertEquals(value, hero);
  }

  @Test
  public void testAvro() throws PulsarClientException {
    Hero hero = Hero.builder().name("Pulsar-Hero").age(28).duty("Save the world").build();
    ProducerBuilder<Hero> producerBuilder = pulsarClient.newProducer(Schema.AVRO(Hero.class));
    Producer<Hero> producer =
        producerBuilder.topic("test-4").producerName("producer-test-4").create();
    producer.send(hero);
    Awaitility.await()
        .untilAsserted(() -> Assertions.assertNotNull(InMemoryStore.cache.get("test-4")));
    Hero value = (Hero) InMemoryStore.cache.get("test-4");
    Assertions.assertEquals(value, hero);
  }

  @Test
  public void testProtoBuf() throws PulsarClientException {
    HeroWrapper.Hero hero =
        HeroWrapper.Hero.newBuilder()
            .setName("Pulsar")
            .setAge(20)
            .setDuty("Save the world")
            .build();
    ProducerBuilder<HeroWrapper.Hero> producerBuilder =
        pulsarClient.newProducer(Schema.PROTOBUF(HeroWrapper.Hero.class));
    Producer<HeroWrapper.Hero> producer =
        producerBuilder.topic("test-5").producerName("producer-test-5").create();
    producer.send(hero);
    Awaitility.await()
        .untilAsserted(() -> Assertions.assertNotNull(InMemoryStore.cache.get("test-5")));
    HeroWrapper.Hero value = (HeroWrapper.Hero) InMemoryStore.cache.get("test-5");
    Assertions.assertEquals(value, hero);
  }
}
