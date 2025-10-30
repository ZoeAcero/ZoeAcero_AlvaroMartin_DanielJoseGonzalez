package org.example.benchmark.service;

import org.example.benchmark.config.TaskExecutorConfig;
import org.example.benchmark.model.BenchmarkResult;
import org.example.benchmark.model.ModeResult;
import org.example.benchmark.task.ComputationTask;
import org.springframework.beans.factory.annotation.Autowired;
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

    // Usamos Inyección por Constructor (más robusta) y final
    private final ThreadPoolTaskExecutor springTaskExecutor;
    private volatile BenchmarkResult lastResult;

    @Autowired
    public BenchmarkService(
            @Qualifier(TaskExecutorConfig.ASYNC_EXECUTOR_NAME) ThreadPoolTaskExecutor springTaskExecutor) {
        // Asignación de dependencia forzada por constructor
        this.springTaskExecutor = springTaskExecutor;
    }

    // --- Métodos de Ejecución ---

    /** MODO 1: Ejecución Secuencial (Monohilo) */
    public long runSequential(int totalTasks) {
        Instant start = Instant.now();
        IntStream.range(0, totalTasks).forEach(i -> {
            try {
                // Ejecución directa en el hilo actual
                new ComputationTask().call();
            } catch (Exception e) {
                // Si falla una tarea, lanzamos una excepción de tiempo de ejecución
                throw new RuntimeException("Error en tarea secuencial", e);
            }
        });
        Instant end = Instant.now();
        return Duration.between(start, end).toMillis();
    }

    /** MODO 2: ExecutorService Manual (FixedThreadPool) */
    public long runExecutorService(int totalTasks, int threads) throws InterruptedException, ExecutionException {
        // Usamos try-with-resources para asegurar que el executor se cierre automáticamente
        try (ExecutorService executor = Executors.newFixedThreadPool(threads)) {
            List<Future<Boolean>> futures = new ArrayList<>();

            Instant start = Instant.now();

            // Enviar todas las tareas al Executor
            for (int i = 0; i < totalTasks; i++) {
                futures.add(executor.submit(new ComputationTask()));
            }

            // Esperar la finalización de CADA tarea. get() lanza la excepción si la tarea falla.
            for (Future<Boolean> future : futures) {
                future.get();
            }

            Instant end = Instant.now();
            return Duration.between(start, end).toMillis();
        }
    }

    /** MODO 3: Spring @Async */
    public long runSpringAsync(int totalTasks, int threads) throws InterruptedException, ExecutionException {
        // Reconfigurar el Executor de Spring con los hilos solicitados
        springTaskExecutor.setMaxPoolSize(threads);
        springTaskExecutor.setCorePoolSize(threads);
        springTaskExecutor.initialize();

        List<Future<Void>> futures = new ArrayList<>();
        Instant start = Instant.now();

        // Enviar las tareas llamando al método @Async
        for (int i = 0; i < totalTasks; i++) {
            futures.add(executeAsyncTask());
        }

        // Esperar la finalización de CADA tarea
        for (Future<Void> future : futures) {
            future.get();
        }

        Instant end = Instant.now();
        return Duration.between(start, end).toMillis();
    }

    /**
     * Método asíncrono que delega la ejecución al ThreadPoolTaskExecutor de Spring.
     */
    @Async(TaskExecutorConfig.ASYNC_EXECUTOR_NAME)
    public Future<Void> executeAsyncTask() {
        try {
            new ComputationTask().call();
        } catch (Exception e) {
            // Manejo de errores: imprime el error y lanza una excepción para que el future.get() falle
            System.err.println("Error en tarea asíncrona: " + e.getMessage());
            throw new RuntimeException("Fallo en tarea asíncrona", e);
        }
        return new AsyncResult<>(null);
    }

    // --- Lógica Principal ---

    public BenchmarkResult runBenchmark(int tasks, int threads) throws ExecutionException, InterruptedException {
        long timeSequential = 0;
        long timeExecutor = 0;
        long timeAsync = 0;

        try {
            // 1. Ejecutar y medir el modo secuencial (T_secuencial)
            timeSequential = runSequential(tasks);

            // 2. Ejecutar y medir el modo ExecutorService (T_executor)
            timeExecutor = runExecutorService(tasks, threads);

            // 3. Ejecutar y medir el modo Spring @Async (T_async)
            timeAsync = runSpringAsync(tasks, threads);

        } catch (RuntimeException | ExecutionException | InterruptedException e) {
            // Este bloque captura cualquier error que se haya propagado y lo registra
            System.err.println("ERROR CRÍTICO AL EJECUTAR BENCHMARK: " + e.getMessage());
            e.printStackTrace();
            // Relanza como un error para que el controlador lo capture y devuelva el Error 500
            throw new RuntimeException("Fallo durante la ejecución del benchmark: " + e.getMessage(), e);
        }

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
        // Cálculo de Speedup (Aceleración)
        double speedup = (timeMs > 0) ? (double) timeSequential / timeMs : 0.0;

        // Cálculo de Efficiency (Eficiencia = Speedup / Hilos)
        double efficiency = (threads > 0) ? speedup / threads : 0.0;

        // Redondear las métricas a dos decimales
        speedup = Math.round(speedup * 100.0) / 100.0;
        efficiency = Math.round(efficiency * 100.0) / 100.0;

        return new ModeResult(mode, timeMs, speedup, efficiency);
    }

    public BenchmarkResult getLastResult() {
        return lastResult;
    }
}
