package com.project.cinebloom.logging;

import com.project.cinebloom.dtos.MovieFormDTO;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

@Aspect
@Component
public class AdminActionLoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(AdminActionLoggingAspect.class);

    public AdminActionLoggingAspect() {
        logger.info("Logging aspect is active");
    }

    @Around("@annotation(com.project.cinebloom.logging.LogAdminAction)")
    public Object logAdminAction(ProceedingJoinPoint joinPoint) throws Throwable {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        LogAdminAction annotation = method.getAnnotation(LogAdminAction.class);
        String action = annotation.action();
        String methodName = method.getName();

        String movieInfo = ""; // Will be filled if it detects a MovieFormDTO
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof MovieFormDTO dto) {
                String title = dto.getTitle();
                Long id = dto.getId();
                if (title != null && id != null) {
                    movieInfo = String.format(" on movie '%s' (id=%d)", title, id);
                } else if (title != null) {
                    movieInfo = String.format(" on movie '%s'", title);
                } else if (id != null) {
                    movieInfo = String.format(" on movie with id=%d", id);
                }
            } else if (arg instanceof Long idArg && methodName.contains("delete")) {
                // For deleteById(Long id)
                movieInfo = String.format(" on movie with id=%d", idArg);
            }
        }

        try {
            Object result = joinPoint.proceed();
            logger.info("Admin '{}' successfully performed {}{}.", username, action, movieInfo);
            return result;
        } catch (Exception ex) {
            logger.error("Admin '{}' FAILED to perform {}{}. Error: {}", username, action, movieInfo, ex.getMessage(), ex);
            throw ex;
        }
    }
}

