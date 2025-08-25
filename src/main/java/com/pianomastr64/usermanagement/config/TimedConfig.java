package com.pianomastr64.usermanagement.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Putting @Timed on arbitrary methods incurs a performance penalty, so keep that in mind.
 */

@Configuration
@Profile({"dev", "metrics"})
public class TimedConfig {
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
}
