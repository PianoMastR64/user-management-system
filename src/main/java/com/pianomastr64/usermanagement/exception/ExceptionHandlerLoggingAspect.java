package com.pianomastr64.usermanagement.exception;

import com.pianomastr64.usermanagement.user.UserRepository;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.util.Optional;

@Aspect
@Component
public class ExceptionHandlerLoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandlerLoggingAspect.class);
    UserRepository userRepository;
    
    @Pointcut("target(GlobalExceptionHandler) && execution(* handle*(..))")
    public void exceptionHandlerMethods() {}
    
    public ExceptionHandlerLoggingAspect(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @AfterReturning(pointcut = "exceptionHandlerMethods()", returning = "result")
    public void logAfterExceptionHandler(JoinPoint joinPoint, Object result) {
        Object[] args = joinPoint.getArgs();
        Exception exception = null;
        WebRequest request = null;
        
        for(Object arg : args) {
            if(arg instanceof Exception ex) {
                exception = ex;
            } else if(arg instanceof WebRequest req) {
                request = req;
            }
        }
        
        if(exception == null) {
            logger.warn("No exception found in join point: {}", joinPoint);
            return;
        }
        
        // Get HTTP request details (if available)
        String path = "N/A";
        String httpMethod = "N/A";
        
        if(request instanceof ServletWebRequest servletWebRequest) {
            path = servletWebRequest.getRequest().getRequestURI();
            httpMethod = servletWebRequest.getRequest().getMethod();
        }
        
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
        
        String summary = "N/A";
        
        if(result instanceof ResponseEntity<?> responseEntity &&
            responseEntity.getBody() instanceof ProblemDetail problemDetail
        ) {
            summary = "Detail: " + problemDetail.getDetail();
            
            Object errors = Optional.ofNullable(problemDetail.getProperties())
                .map(properties -> properties.get("errors"))
                .orElse(null);
            if(errors != null) {
                summary += "; Errors: " + errors;
            }
        }
        
        logger.warn("""
                Exception handled: {}: {}
                    Request: {} {}
                    User: {}
                    Summary: "{}"\
                """,
            exception.getClass().getSimpleName(), exception.getMessage(),
            httpMethod, path,
            user,
            summary);
        
        if(result instanceof ResponseEntity<?> responseEntity) {
            if(responseEntity.getStatusCode().is5xxServerError()) {
                logger.error("Stack trace for unhandled exception:", exception);
            }
        }
    }
    
}
