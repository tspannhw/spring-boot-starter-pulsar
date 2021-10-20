package com.github.pulsar.eco.spring.starter.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "pulsar.client")
public class PulsarProperties {

}
