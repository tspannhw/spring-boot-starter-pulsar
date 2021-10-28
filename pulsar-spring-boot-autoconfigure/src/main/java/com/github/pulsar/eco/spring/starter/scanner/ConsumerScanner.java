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

package com.github.pulsar.eco.spring.starter.scanner;

import com.github.pulsar.eco.spring.starter.annotation.PulsarHandler;
import com.github.pulsar.eco.spring.starter.annotation.PulsarListener;
import com.github.pulsar.eco.spring.starter.constant.Symbol;
import com.github.pulsar.eco.spring.starter.exception.PulsarClientConfigException;
import com.github.pulsar.eco.spring.starter.exception.PulsarDuplicatedConsumerNameException;
import com.github.pulsar.eco.spring.starter.modal.Consumer;
import com.google.common.collect.Maps;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Consumer Scanner.
 *
 * <p>Use this class to store all consumer and delegate all request to consumer.
 *
 * @since 2.8.1
 * @author mattison
 */
@Component
@Slf4j
@DependsOn({"pulsarClient"})
public class ConsumerScanner implements BeanPostProcessor {

  private final Map<String, Consumer> container = Maps.newConcurrentMap();

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    return bean;
  }

  /**
   * Override this method to find all consumer and store in container.
   *
   * @param bean - Spring Bean
   * @param beanName - Spring Bean Name
   * @return Bean
   * @throws BeansException - Spring Beans Exception
   */
  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName)
      throws BeansException {
    final Class<?> klass = bean.getClass();
    PulsarListener pulsarListener = klass.getDeclaredAnnotation(PulsarListener.class);
    if (pulsarListener != null) {
      List<Method> methods =
          Arrays.stream(klass.getDeclaredMethods())
              .filter(method -> method.isAnnotationPresent(PulsarHandler.class))
              .collect(Collectors.toList());
      if (CollectionUtils.isEmpty(methods)) {
        throw new PulsarClientConfigException(
            String.format(
                "Do not find handler in %s listener, maybe you forget to annotate it?", beanName));
      }
      if (methods.size() > 1) {
        String methodNames = methods.stream().map(Method::getName).collect(Collectors.joining(","));
        throw new PulsarClientConfigException(
            String.format(
                "Find more than 1 handler in listener, the method name is %s", methodNames));
      }
      stuffConsumerToContainer(bean, methods.get(0), pulsarListener);
    } else {
      Arrays.stream(klass.getDeclaredMethods())
          .filter(method -> method.isAnnotationPresent(PulsarListener.class))
          .forEach(
              method ->
                  stuffConsumerToContainer(
                      bean, method, method.getDeclaredAnnotation(PulsarListener.class)));
    }
    return bean;
  }

  /**
   * Assemble consumer and then store in container.
   *
   * @param bean - Spring Bean
   * @param method - Java Method
   * @param pulsarListener - Pulsar listener annotation
   * @see PulsarListener
   */
  private void stuffConsumerToContainer(Object bean, Method method, PulsarListener pulsarListener) {
    Consumer consumer =
        Consumer.builder().listener(pulsarListener).handler(method).delegator(bean).build();
    for (String topicName : pulsarListener.topicNames()) {
      String uniqueKey = topicName + Symbol.SLASH + pulsarListener.consumerName();
      Consumer absentConsumer = container.putIfAbsent(uniqueKey, consumer);
      log.debug("{} Load consumer {} successful.", Symbol.PREFIX_EMOJI, uniqueKey);
      if (absentConsumer != null) {
        throw new PulsarDuplicatedConsumerNameException(
            String.format(
                "The consumer name must be unique in same topic, the duplicated consumer name is %s",
                pulsarListener.consumerName()));
      }
    }
  }

  /**
   * Get consumer by key.
   *
   * @param consumerKey - key
   * @return Consumer - Optional[Consumer]
   */
  public Optional<Consumer> getConsumerByKey(String consumerKey) {
    Consumer consumer = container.get(consumerKey);
    return Optional.ofNullable(consumer);
  }

  /**
   * Get container
   *
   * @return container
   */
  public Map<String, Consumer> getContainer() {
    return container;
  }
}
