package app.config;

import app.exception.AsyncExceptionHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.Executor;

/**
 * Configuration for scheduled tasks and async execution.
 * Configures thread pools for both scheduling and async task execution.
 */
@Configuration
@Slf4j
@RequiredArgsConstructor
public class SchedulingConfig implements SchedulingConfigurer, AsyncConfigurer {

    private final AsyncExceptionHandler asyncExceptionHandler;

    @Value("${scheduling.pool.size:5}")
    private int schedulingPoolSize;

    @Value("${scheduling.async.core-pool-size:4}")
    private int asyncCorePoolSize;

    @Value("${scheduling.async.max-pool-size:10}")
    private int asyncMaxPoolSize;

    @Value("${scheduling.async.queue-capacity:100}")
    private int asyncQueueCapacity;

    /**
     * Configure the task scheduler for @Scheduled methods.
     * Uses a ThreadPoolTaskScheduler for better performance than single-threaded default.
     */
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setTaskScheduler(taskScheduler());
    }

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(schedulingPoolSize);
        scheduler.setThreadNamePrefix("scheduled-task-");
        scheduler.setErrorHandler(throwable -> 
            log.error("Error in scheduled task: {}", throwable.getMessage(), throwable));
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);
        scheduler.initialize();
        return scheduler;
    }

    /**
     * Configure the executor for @Async methods.
     * Provides a configurable thread pool for async task execution.
     */
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(asyncCorePoolSize);
        executor.setMaxPoolSize(asyncMaxPoolSize);
        executor.setQueueCapacity(asyncQueueCapacity);
        executor.setThreadNamePrefix("async-task-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return asyncExceptionHandler;
    }
}
