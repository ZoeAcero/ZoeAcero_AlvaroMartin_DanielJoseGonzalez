package org.example.benchmark.model;

import java.util.List;

public class BenchmarkResult {
    private int totalTasks;
    private int threadsUsed;
    private List<ModeResult> results;

    // Constructor vacío
    public BenchmarkResult() {}

    // Constructor completo
    public BenchmarkResult(int totalTasks, int threadsUsed, List<ModeResult> results) {
        this.totalTasks = totalTasks;
        this.threadsUsed = threadsUsed;
        this.results = results;
    }

    // Getters y Setters
    public int getTotalTasks() { return totalTasks; }
    public void setTotalTasks(int totalTasks) { this.totalTasks = totalTasks; }
    public int getThreadsUsed() { return threadsUsed; }
    public void setThreadsUsed(int threadsUsed) { this.threadsUsed = threadsUsed; }
    public List<ModeResult> getResults() { return results; }
    public void setResults(List<ModeResult> results) { this.results = results; }
}