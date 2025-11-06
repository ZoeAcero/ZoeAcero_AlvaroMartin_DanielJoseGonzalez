package org.example.benchmark.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync  // habilita el soporte para métodos @Async
public class TaskExecutorConfig {

    // Nombre constante para reutilizarlo en @Qualifier y @Async
    public static final String ASYNC_EXECUTOR_NAME = "springAsyncTaskExecutor";

    @Bean(name = ASYNC_EXECUTOR_NAME)
    public ThreadPoolTaskExecutor springAsyncTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("AsyncExecutor-");
        executor.initialize();
        return executor;
    }
}
