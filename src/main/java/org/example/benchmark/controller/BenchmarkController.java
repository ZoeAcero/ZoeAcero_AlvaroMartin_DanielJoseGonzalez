package org.example.benchmark.controller;

import com.ejemplo.benchmark.model.BenchmarkResult;
import com.ejemplo.benchmark.service.BenchmarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/benchmark")
public class BenchmarkController {

    @Autowired
    private BenchmarkService benchmarkService;

    /**
     * POST /benchmark/start: Inicia una prueba de rendimiento con parámetros.
     * Ejemplo: POST /benchmark/start?tasks=50&threads=8
     */
    @PostMapping("/start")
    public ResponseEntity<BenchmarkResult> startBenchmark(
        @RequestParam(defaultValue = "50") int tasks,
        @RequestParam(defaultValue = "4") int threads) throws ExecutionException, InterruptedException {

        if (tasks <= 0 || threads <= 0) {
            return ResponseEntity.badRequest().body(null);
        }

        System.out.println("Iniciando benchmark con " + tasks + " tareas y " + threads + " hilos.");
        
        BenchmarkResult result = benchmarkService.runBenchmark(tasks, threads);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /benchmark/result: Devuelve los resultados del último test ejecutado.
     */
    @GetMapping("/result")
    public ResponseEntity<BenchmarkResult> getResult() {
        BenchmarkResult result = benchmarkService.getLastResult();
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    /**
     * GET /benchmark/modes: Lista los modos de ejecución disponibles.
     */
    @GetMapping("/modes")
    public ResponseEntity<Map<String, String>> getModes() {
        Map<String, String> modes = Map.of(
            "SEQUENTIAL", "Ejecución monohilo en el hilo principal.",
            "EXECUTOR_SERVICE", "Ejecución concurrente con ExecutorService (FixedThreadPool) manual.",
            "SPRING_ASYNC", "Ejecución asíncrona usando métodos @Async y ThreadPoolTaskExecutor de Spring."
        );
        return ResponseEntity.ok(modes);
    }
}