package com.github.pulsar.eco.spring.starter;

import com.github.pulsar.eco.spring.starter.properties.PulsarProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
@EnableConfigurationProperties({PulsarProperties.class})
public class PulsarAutoConfigure {
    private final PulsarProperties pulsarProperties;

    public PulsarAutoConfigure(PulsarProperties pulsarProperties) {
        this.pulsarProperties = pulsarProperties;
    }
}
