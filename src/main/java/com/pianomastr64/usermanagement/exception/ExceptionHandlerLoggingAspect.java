package com.pianomastr64.usermanagement.exception;

import com.pianomastr64.usermanagement.user.UserRepository;
import jakarta.validation.ConstraintViolationException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class ExceptionHandlerLoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandlerLoggingAspect.class);
    UserRepository userRepository;
    
    @Pointcut("within(@org.springframework.web.bind.annotation.RestControllerAdvice *) && " +
        "@annotation(org.springframework.web.bind.annotation.ExceptionHandler)")
    public void exceptionHandlerMethods() {}
    
    public ExceptionHandlerLoggingAspect(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @AfterReturning(pointcut = "exceptionHandlerMethods()", returning = "result")
    public void logAfterExceptionHandler(JoinPoint joinPoint, Object result) {
        Object[] args = joinPoint.getArgs();
        if(args.length > 0 && args[0] instanceof Exception ex) {
            // Get HTTP request details (if available)
            String path = "N/A";
            String httpMethod = "N/A";
            try {
                ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if(attrs != null) {
                    path = attrs.getRequest().getRequestURI();
                    httpMethod = attrs.getRequest().getMethod();
                }
            } catch(Exception ignored) {}
            
            // Get user details from Spring Security (if available)
            String user = "Anonymous";
            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if(authentication != null && authentication.isAuthenticated()) {
                    Long id = Long.valueOf(authentication.getName());
                    user = userRepository.findById(id)
                        .map(u -> u.getName() + " (" + u.getEmail() + ")")
                        .orElse("Unknown User");
                }
            } catch(Exception ignored) {}
            
            String summary = switch(ex) {
                case MethodArgumentNotValidException e -> e.getBindingResult().getFieldErrors().stream()
                    .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                    .reduce((msg1, msg2) -> msg1 + "; " + msg2)
                    .orElse("Validation failed");
                case ConstraintViolationException e -> e.getConstraintViolations().stream()
                    .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                    .reduce((msg1, msg2) -> msg1 + "; " + msg2)
                    .orElse("Constraint violations");
                default -> "N/A";
            };
            
            logger.warn("""
                    Exception handled: {}: {}
                        Request: {} {}
                        User: {}
                        Summary: "{}"\
                    """,
                ex.getClass().getSimpleName(), ex.getMessage(),
                httpMethod, path,
                user,
                summary);
            
            if(result instanceof ResponseEntity<?> responseEntity) {
                if(responseEntity.getStatusCode().is5xxServerError()) {
                    logger.error("Stack trace for unhandled exception:", ex);
                }
            }
        }
    }
    
}
