package org.example.benchmark.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;

@Configuration
@EnableAsync // Habilita el procesamiento de las anotaciones @Async
public class TaskExecutorConfig {

    public static final String ASYNC_EXECUTOR_NAME = "springAsyncTaskExecutor";
    /**
     * Define y configura el Executor (ThreadPoolTaskExecutor) que Spring usará
     * para los métodos anotados con @Async.
     */
    @Bean(name = ASYNC_EXECUTOR_NAME)
    public Executor springAsyncTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Estos son valores por defecto; se reconfiguran dinámicamente en BenchmarkService
        executor.setCorePoolSize(4); 
        executor.setMaxPoolSize(8); 
        
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("SpringAsync-");
        executor.initialize();
        return executor;
    }
}