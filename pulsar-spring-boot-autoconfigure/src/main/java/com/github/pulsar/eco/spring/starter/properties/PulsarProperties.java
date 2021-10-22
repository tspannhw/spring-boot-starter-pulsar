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
package com.github.pulsar.eco.spring.starter.properties;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Pulsar properties.
 *
 * <p>Copy it from docs.
 *
 * @see <a href="https://pulsar.apache.org/docs/en/client-libraries-java/">Apache pulsar Client
 *     doc</a>
 * @since 2.8.1
 * @author mattison
 */
@Data
@ConfigurationProperties(prefix = "pulsar.client")
public class PulsarProperties {
  /* Service URL provider for Pulsar service */
  private String serviceUrl;
  /* Name of the authentication plugin */
  private String authPluginClassName;
  /**
   * String represents parameters for the authentication plugin
   *
   * <p>Example key1:val1,key2:val2
   */
  private Map<String, String> authParams;
  /* Operation timeout */
  private Integer operationTimeoutMs = 30000;
  /**
   * Interval between each status info
   *
   * <p>Stats is activated with positive statsInterval
   *
   * <p>Set statsIntervalSeconds to 1 second at least
   */
  private Long statsIntervalSeconds = 60L;
  /* The number of threads used for handling connections to brokers */
  private Integer numIoThreads = 1;
  /**
   * The number of threads used for handling message listeners. The listener thread pool is shared
   * across all the consumers and readers using the "listener" model to get messages. For a given
   * consumer, the listener is always invoked from the same thread to ensure ordering. If you want
   * multiple threads to process a single topic, you need to create a shared subscription and
   * multiple consumers for this subscription. This does not ensure ordering.
   */
  private Integer numListenerThreads = 1;
  /* Whether to use TCP no-delay flag on the connection to disable Nagle algorithm */
  private Boolean useTcpNoDelay = true;
  /* Whether to use TLS encryption on the connection */
  private Boolean useTls = false;
  /* Path to the trusted TLS certificate file */
  private String tlsTrustCertsFilePath;
  /* Whether the Pulsar client accepts untrusted TLS certificate from broker */
  private Boolean tlsAllowInsecureConnection = false;
  /* Whether to enable TLS hostname verification */
  private Boolean tlsHostnameVerificationEnable = false;
  /**
   * The number of concurrent lookup requests allowed to send on each broker connection to prevent
   * overload on broker.
   */
  private Integer concurrentLookupRequest = 5000;
  /**
   * The maximum number of lookup requests allowed on each broker connection to prevent overload on
   * broker
   */
  private Integer maxLookupRequest = 50000;
  /**
   * The maximum number of rejected requests of a broker in a certain time frame (30 seconds) after
   * the current connection is closed and the client creates a new connection to connect to a
   * different broker
   */
  private Integer maxNumberOfRejectedRequestPerConnection = 50;
  /* Seconds of keeping alive interval for each client broker connection	*/
  private Integer keepAliveIntervalSeconds = 30;
  /**
   * Duration of waiting for a connection to a broker to be established
   *
   * <p>If the duration passes without a response from a broker, the connection attempt is dropped
   */
  private Integer connectionTimeoutMs = 10000;
  /* Maximum duration for completing a request */
  private Integer requestTimeoutMs = 60000;
  /* Default duration for a backoff interval */
  private Integer defaultBackoffIntervalNanos = Math.toIntExact(TimeUnit.MILLISECONDS.toNanos(100));
  ;
  /* Maximum duration for a backoff interval */
  private Long maxBackoffIntervalNanos = TimeUnit.SECONDS.toNanos(30);
  /* SOCKS5 proxy address */
  private SocketAddress socks5ProxyAddress;
  /* SOCKS5 proxy username */
  private String socks5ProxyUsername;
  /* SOCKS5 proxy password */
  private String socks5ProxyPassword;
}
