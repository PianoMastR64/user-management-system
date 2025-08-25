package com.pianomastr64.usermanagement.instrumentation;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "perf.monitoring")
public class PerformanceMonitoringProperties {
    /**
     * Enables or disables the performance monitoring aspect.
     */
    private boolean enabled;
    
    /**
     * Enables or disables logging of performance metrics to the console.
     */
    private boolean log = true;
    
    public boolean isEnabled() {return enabled;}
    
    public void setEnabled(boolean enabled) {this.enabled = enabled;}
    
    public boolean isLog() {return log;}
    
    public void setLog(boolean log) {this.log = log;}
}

