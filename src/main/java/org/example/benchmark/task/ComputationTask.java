package org.example.benchmark.task;

import java.util.concurrent.Callable;

/**
 * Tarea CPU-intensiva simulada: calcula primos hasta un número fijo.
 */
public class ComputationTask implements Callable<Boolean> {

    // Lógica intensiva: función auxiliar para verificar si un número es primo
    private boolean isPrime(long number) {
        if (number <= 1) return false;
        // Solo necesitamos verificar hasta la raíz cuadrada del número
        for (int i = 2; i * i <= number; i++) {
            if (number % i == 0) return false;
        }
        return true;
    }

    @Override
    public Boolean call() throws Exception {
        // Ejecución de la tarea CPU-intensiva
        // Se reduce el target a 15000 para evitar Timeouts (Error 500)
        long target = 15000;
        int count = 0;

        // Bucle que realiza el cálculo intensivo
        for (long i = 1; i <= target; i++) {
            if (isPrime(i)) {
                count++;
            }
        }

        // Devolvemos true, pero el resultado real es el tiempo consumido.
        return true;
    }
}
