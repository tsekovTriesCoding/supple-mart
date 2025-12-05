package app.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Handler for uncaught exceptions in @Async methods.
 * Provides detailed logging for async execution failures.
 */
@Component
@Slf4j
public class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

    @Override
    public void handleUncaughtException(Throwable ex, Method method, Object... params) {
        log.error("Async exception in method '{}' of class '{}': {}",
                method.getName(),
                method.getDeclaringClass().getSimpleName(),
                ex.getMessage());

        if (params.length > 0) {
            log.error("Method parameters: {}", Arrays.toString(params));
        }

        log.error("Stack trace:", ex);
    }
}

