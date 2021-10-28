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

package com.github.pulsar.eco.spring.starter.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import com.github.pulsar.eco.spring.starter.modal.RuntimeResult;

public class JavaCommandUtils {

  public static RuntimeResult exec(String commands) {
    try {
      try {
        Process exec = Runtime.getRuntime().exec(commands);
        int code = exec.waitFor();
        if (code == 0) {
          try (BufferedReader bufferedReader =
              new BufferedReader(new InputStreamReader(exec.getInputStream()))) {
            String outPutMsg = bufferedReader.lines().collect(Collectors.joining("\n"));
            return RuntimeResult.builder().success(true).description(outPutMsg).build();
          }
        }
        try (BufferedReader bufferedReader =
            new BufferedReader(new InputStreamReader(exec.getErrorStream()))) {
          String errMsg = bufferedReader.lines().collect(Collectors.joining("\n"));
          return RuntimeResult.builder().success(false).description(errMsg).build();
        }
      } catch (InterruptedException e) {
        return RuntimeResult.builder()
            .success(false)
            .description(String.format("Execute command interrupted %s .", commands))
            .build();
      }
    } catch (IOException e) {
      return RuntimeResult.builder()
          .success(false)
          .description(String.format("Execute command %s fail.", commands))
          .build();
    }
  }
}
