package ru.practicum.explore_with_me.ewm.api.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import ru.practicum.explore_with_me.stat.api.dto.EndpointHit;
import ru.practicum.explore_with_me.stat.client.StatClient;

import java.time.LocalDateTime;

@Slf4j
@Aspect
@RequiredArgsConstructor
@Component
public class StatisticSender {
    private static final String SERVICE_NAME = "ewm-main-service";
    private final StatClient statClient;

    @Pointcut("execution(public * ru.practicum.explore_with_me.ewm.api.*.*(..))")
    public void apiInvocationMethods() {
    }

    @Before(value = "apiInvocationMethods()")
    public void logMethodExecution(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof HttpServletRequest request) {
                try {
                    String addr = request.getRemoteAddr();
                    String requestURI = request.getRequestURI();
                    EndpointHit endpointHit = new EndpointHit();
                    endpointHit.setApp(SERVICE_NAME);
                    endpointHit.setUri(requestURI);
                    endpointHit.setIp(addr);
                    endpointHit.setTimestamp(LocalDateTime.now());
                    statClient.saveHit(endpointHit);
                } catch (Exception e) {
                    log.error("Ошибка при сохранении данных для статистики", e);
                }
            }
        }
    }
}
