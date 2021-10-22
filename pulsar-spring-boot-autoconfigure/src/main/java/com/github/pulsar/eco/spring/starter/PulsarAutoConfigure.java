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
package com.github.pulsar.eco.spring.starter;

import com.github.pulsar.eco.spring.starter.exception.PulsarClientConfigException;
import com.github.pulsar.eco.spring.starter.properties.PulsarProperties;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.apache.pulsar.client.api.ClientBuilder;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

@Configuration
@ComponentScan
@EnableConfigurationProperties({PulsarProperties.class})
public class PulsarAutoConfigure {
  private final PulsarProperties pulsarProperties;

  public PulsarAutoConfigure(PulsarProperties pulsarProperties) {
    this.pulsarProperties = pulsarProperties;
  }

  @Bean
  @ConditionalOnMissingBean
  public PulsarClient pulsarClient() throws PulsarClientException {
    ClientBuilder clientBuilder =
        PulsarClient.builder()
            .serviceUrl(pulsarProperties.getServiceUrl())
            .operationTimeout(pulsarProperties.getOperationTimeoutMs(), TimeUnit.MILLISECONDS)
            .statsInterval(pulsarProperties.getStatsIntervalSeconds(), TimeUnit.SECONDS)
            .ioThreads(pulsarProperties.getNumIoThreads())
            .listenerThreads(pulsarProperties.getNumListenerThreads())
            .enableTcpNoDelay(pulsarProperties.getUseTcpNoDelay())
            .tlsTrustCertsFilePath(pulsarProperties.getTlsTrustCertsFilePath())
            .allowTlsInsecureConnection(pulsarProperties.getTlsAllowInsecureConnection())
            .enableTlsHostnameVerification(pulsarProperties.getTlsHostnameVerificationEnable())
            .maxConcurrentLookupRequests(pulsarProperties.getConcurrentLookupRequest())
            .maxLookupRequests(pulsarProperties.getMaxLookupRequest())
            .maxNumberOfRejectedRequestPerConnection(
                pulsarProperties.getMaxNumberOfRejectedRequestPerConnection())
            .keepAliveInterval(pulsarProperties.getKeepAliveIntervalSeconds(), TimeUnit.SECONDS)
            .connectionTimeout(pulsarProperties.getConnectionTimeoutMs(), TimeUnit.MILLISECONDS)
            .startingBackoffInterval(
                pulsarProperties.getDefaultBackoffIntervalNanos(), TimeUnit.NANOSECONDS)
            .maxBackoffInterval(
                pulsarProperties.getMaxBackoffIntervalNanos(), TimeUnit.NANOSECONDS);
    // check auth plugin
    boolean hasAuthPluginClassName =
        StringUtils.isNotBlank(pulsarProperties.getAuthPluginClassName());
    boolean hasAuthParams = !CollectionUtils.isEmpty(pulsarProperties.getAuthParams());
    if (hasAuthPluginClassName && hasAuthParams) {
      try {
        clientBuilder.authentication(
            pulsarProperties.getAuthPluginClassName(), pulsarProperties.getAuthParams());
      } catch (PulsarClientException.UnsupportedAuthenticationException e) {
        throw new PulsarClientConfigException(
            String.format(
                "Unsupported Authentication  the AuthPluginClassName is %s, the AuthParams is %s",
                pulsarProperties.getAuthPluginClassName(), pulsarProperties.getAuthParams()),
            e);
      }
    } else {
      if (hasAuthPluginClassName) {
        throw new PulsarClientConfigException("Do not get authParams, did you forget it ? ");
      }
      if (hasAuthParams) {
        throw new PulsarClientConfigException(
            "Do not get authPluginClassName, did you forget it ? ");
      }
    }
    // check tls trust certs file path
    if (pulsarProperties.getTlsTrustCertsFilePath() != null) {
      clientBuilder.tlsTrustCertsFilePath(pulsarProperties.getTlsTrustCertsFilePath());
    }
    return clientBuilder.build();
  }
}
