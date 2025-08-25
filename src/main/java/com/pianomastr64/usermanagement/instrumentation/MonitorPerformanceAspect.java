package com.pianomastr64.usermanagement.instrumentation;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.env.Profiles;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Aspect to monitor method performance using <a href="https://micrometer.io/">Micrometer</a>.
 *
 * <p>
 * (I realized later that you can use the {@link io.micrometer.core.annotation.Timed @Timed} annotation and pointcut it with logs.
 * Though this was good for learning how to use AOP and Micrometer. So I deprecated {@link MonitorPerformance @MonitorPerformance}.)
 */

@Aspect
@Component
// dev/metrics profile or perf.monitoring.enabled=true
@Conditional(MonitorPerformanceAspect.MonitoringEnabledCondition.class)
@ConditionalOnClass(MeterRegistry.class)
public class MonitorPerformanceAspect {
    
    private static final Logger log = LoggerFactory.getLogger(MonitorPerformanceAspect.class);
    
    private final MeterRegistry registry;
    private final PerformanceMonitoringProperties properties;
    
    private record Meta(String tag, boolean warnOnly, long thresholdMs, Timer timer) {}
    private final ConcurrentHashMap<Method, Meta> metaCache = new ConcurrentHashMap<>();
    
    @Autowired
    public MonitorPerformanceAspect(
        MeterRegistry registry,
        PerformanceMonitoringProperties properties
    ) {
        this.registry = registry;
        this.properties = properties;
    }
    
    private Meta getMeta(Method method, ProceedingJoinPoint pjp) {
        return metaCache.computeIfAbsent(method, key -> {
            MonitorPerformance ann = AnnotatedElementUtils.findMergedAnnotation(key, MonitorPerformance.class);
            if(ann == null) {
                ann = AnnotatedElementUtils.findMergedAnnotation(key.getDeclaringClass(), MonitorPerformance.class);
            }
            
            String tag = ann != null && !ann.value().isEmpty()
                ? ann.value()
                : pjp.getSignature().getDeclaringType().getSimpleName();
            
            boolean warnOnly = ann != null && ann.warnOnly();
            long thresholdMs = ann != null ? ann.thresholdMs() : MonitorPerformance.DEFAULT_THRESHOLD;
            
            Timer timer = registry.timer(
                "application.method.timing",
                "bean", tag,
                "method", key.getName());
            
            return new Meta(tag, warnOnly, thresholdMs, timer);
        });
    }
    
    @Pointcut("execution(public * *(..)) && (@within(MonitorPerformance) || @annotation(MonitorPerformance))")
    public void monitored() {}
    
    @Around("monitored()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        Meta meta = getMeta(method, pjp);
        
        Timer.Sample sample = Timer.start(registry);
        try {
            return pjp.proceed();
        } finally {
            
            long elapsedNanos = sample.stop(meta.timer());
            long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(elapsedNanos);
            
            if(properties.isLog()){
                logPerformance(meta, elapsedMillis);
            }
        }
    }
    
    private void logPerformance(Meta meta, long elapsedMillis) {
        if(meta.warnOnly()) {
            if(elapsedMillis > meta.thresholdMs()) {
                log.warn("[perf] {} took {} ms (>{} ms)",
                    meta.tag(), elapsedMillis, meta.thresholdMs());
            }
        } else {
            log.debug("[perf] {} took {} ms",
                meta.tag(), elapsedMillis);
        }
    }
    
    static class MonitoringEnabledCondition extends AnyNestedCondition {
        MonitoringEnabledCondition() {
            super(ConfigurationPhase.REGISTER_BEAN);
        }
        
        @ConditionalOnProperty(name = "perf.monitoring.enabled", havingValue = "true")
        static class PropertyEnabledCondition {}
        
        @Conditional(ProfilesActive.class)
        static class ProfilesActiveCondition {}
        
        static class ProfilesActive implements Condition {
            @Override
            public boolean matches(ConditionContext context, @Nullable AnnotatedTypeMetadata metadata) {
                return context.getEnvironment().acceptsProfiles(Profiles.of(
                    "dev", "metrics"));
            }
        }
    }
}
