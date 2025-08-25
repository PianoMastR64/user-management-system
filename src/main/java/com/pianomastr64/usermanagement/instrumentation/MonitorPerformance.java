package com.pianomastr64.usermanagement.instrumentation;

import java.lang.annotation.*;

/**
 * Mark a class or method to have execution time recorded.
 *
 * <p>By default, all public methods of an annotated class are measured.
 * If you put the annotation on individual methods, only those are measured.</p>
 *
 * <p>This annotation is only effective if performance monitoring is enabled.
 * Monitoring is enabled when either the {@code perf.monitoring.enabled} property is set to {@code true},
 * or the active Spring profile is {@code dev} or {@code metrics}.</p>
 *
 * <p>If {@code warnOnly} is set to {@code true}, only WARN logs are emitted when execution time exceeds {@code thresholdMs} (in milliseconds);
 * otherwise, all executions are logged at INFO level. Metrics are always recorded if Micrometer is available.</p>
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Deprecated
public @interface MonitorPerformance {
    long DEFAULT_THRESHOLD = 500;

    /**
     * Optional tag you’ll see in metrics/logs (defaults to the class or method name).
     */
    String value() default "";

    /**
     * Set to {@code true} to emit WARN logs only when execution time exceeds {@link #thresholdMs()}.
     * If {@code false}, all executions are logged at DEBUG level.
     */
    boolean warnOnly() default false;

    /**
     * Threshold in milliseconds for warnOnly mode.
     */
    long thresholdMs() default DEFAULT_THRESHOLD;
}
