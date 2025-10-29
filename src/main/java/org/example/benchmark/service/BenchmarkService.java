package org.example.benchmark.service;

import org.example.benchmark.config.TaskExecutorConfig;
import org.example.benchmark.model.BenchmarkResult;
import org.example.benchmark.model.ModeResult;
import org.example.benchmark.task.ComputationTask;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

@Service
public class BenchmarkService {



    @Qualifier(TaskExecutorConfig.ASYNC_EXECUTOR_NAME)
    private ThreadPoolTaskExecutor springTaskExecutor;

    // Almacena el resultado del último test para el endpoint GET /result
    private volatile BenchmarkResult lastResult;

    // --- Métodos de Ejecución ---

    /** MODO 1: Ejecución Secuencial (Monohilo) */
    public long runSequential(int totalTasks) {
        Instant start = Instant.now();
        // Recorre y ejecuta cada tarea en el hilo principal
        IntStream.range(0, totalTasks).forEach(i -> {
            try {
                new ComputationTask().call();
            } catch (Exception e) {
                // Interrumpe el hilo actual en caso de error
                Thread.currentThread().interrupt();
            }
        });
        Instant end = Instant.now();
        return Duration.between(start, end).toMillis();
    }

    /** MODO 2: ExecutorService Manual (FixedThreadPool) */
    public long runExecutorService(int totalTasks, int threads) throws InterruptedException, ExecutionException {
        // Crea un pool de hilos con el número especificado (P)
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<Future<Boolean>> futures = new ArrayList<>();

        Instant start = Instant.now();

        // Enviar todas las tareas al Executor
        for (int i = 0; i < totalTasks; i++) {
            futures.add(executor.submit(new ComputationTask()));
        }

        // Esperar (bloquear) por la finalización de CADA tarea
        for (Future<Boolean> future : futures) {
            future.get(); // Bloquea hasta que la tarea termine
        }

        Instant end = Instant.now();
        executor.shutdown(); // Importante liberar recursos
        return Duration.between(start, end).toMillis();
    }

    /** MODO 3: Spring @Async */
    public long runSpringAsync(int totalTasks, int threads) throws InterruptedException, ExecutionException {

        // **IMPORTANTE**: Reconfigurar el Executor de Spring con los hilos solicitados
        springTaskExecutor.setMaxPoolSize(threads);
        springTaskExecutor.setCorePoolSize(threads);
        // Inicializar el ThreadPoolTaskExecutor con los nuevos tamaños de pool
        springTaskExecutor.initialize();

        List<Future<Void>> futures = new ArrayList<>();
        Instant start = Instant.now();

        // Enviar las tareas llamando al método @Async
        for (int i = 0; i < totalTasks; i++) {
            futures.add(executeAsyncTask());
        }

        // Esperar (bloquear) por la finalización de CADA tarea
        for (Future<Void> future : futures) {
            future.get();
        }

        Instant end = Instant.now();
        return Duration.between(start, end).toMillis();
    }

    /**
     * Método asíncrono que delega la ejecución al ThreadPoolTaskExecutor de Spring.
     * Debe devolver Future<> para poder esperar su finalización.
     */
    @Async(TaskExecutorConfig.ASYNC_EXECUTOR_NAME)
    public Future<Void> executeAsyncTask() {
        try {
            new ComputationTask().call();
        } catch (Exception e) {
            // Manejo de errores en tareas asíncronas
            System.err.println("Error en tarea asíncrona: " + e.getMessage());
        }
        return new AsyncResult<>(null); // Devuelve un Future vacío para indicar finalización
    }

    // --- Lógica Principal ---

    public BenchmarkResult runBenchmark(int tasks, int threads) throws ExecutionException, InterruptedException {

        // 1. Ejecutar y medir el modo secuencial (T_secuencial)
        long timeSequential = runSequential(tasks);

        // 2. Ejecutar y medir el modo ExecutorService (T_executor)
        long timeExecutor = runExecutorService(tasks, threads);

        // 3. Ejecutar y medir el modo Spring @Async (T_async)
        long timeAsync = runSpringAsync(tasks, threads);

        // 4. Calcular métricas
        ModeResult seqResult = calculateMetrics("SEQUENTIAL", timeSequential, timeSequential, 1);
        ModeResult execResult = calculateMetrics("EXECUTOR_SERVICE", timeExecutor, timeSequential, threads);
        ModeResult asyncResult = calculateMetrics("SPRING_ASYNC", timeAsync, timeSequential, threads);

        // 5. Ensamblar y almacenar el resultado final
        BenchmarkResult result = new BenchmarkResult(
                tasks,
                threads,
                List.of(seqResult, execResult, asyncResult)
        );

        this.lastResult = result;
        return result;
    }

    // Método auxiliar para el cálculo de métricas
    private ModeResult calculateMetrics(String mode, long timeMs, long timeSequential, int threads) {
        // Evitar división por cero si T_concurrente fuera 0 (caso teórico)
        double speedup = (timeMs > 0) ? (double) timeSequential / timeMs : Double.POSITIVE_INFINITY;

        // Eficiencia: Speedup / Número de hilos
        double efficiency = (threads > 0) ? speedup / threads : 0.0;

        // Redondear las métricas a dos decimales para el JSON
        speedup = Math.round(speedup * 100.0) / 100.0;
        efficiency = Math.round(efficiency * 100.0) / 100.0;

        return new ModeResult(mode, timeMs, speedup, efficiency);
    }

    public BenchmarkResult getLastResult() {
        return lastResult;
    }
}
