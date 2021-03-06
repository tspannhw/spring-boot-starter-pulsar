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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that marks a method to be the target of a Pulsar message listener within a class that
 * is annotated with {@link PulsarListener}.
 *
 * <p>See the {@link PulsarListener} for information about permitted method signatures and available
 * parameters.
 *
 * <p><b>It is important to understand that when a message arrives, the method selection depends on
 * the payload type. The type is matched with a single non-annotated parameter, or one that is
 * annotated with {@code @Payload}. There must be no ambiguity - the system must be able to select
 * exactly one method based on the payload type.</b>
 *
 * @author mattison
 * @since 1.0.0
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PulsarHandler {}
