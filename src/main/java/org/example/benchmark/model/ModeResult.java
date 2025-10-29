package org.example.benchmark.model;

public class ModeResult {
    private String mode;
    private long timeMs;
    private double speedup;
    private double efficiency;

    // Constructor vacío (necesario para frameworks como Jackson/Spring)
    public ModeResult() {}

    // Constructor completo
    public ModeResult(String mode, long timeMs, double speedup, double efficiency) {
        this.mode = mode;
        this.timeMs = timeMs;
        this.speedup = speedup;
        this.efficiency = efficiency;
    }

    // Getters y Setters
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public long getTimeMs() { return timeMs; }
    public void setTimeMs(long timeMs) { this.timeMs = timeMs; }
    public double getSpeedup() { return speedup; }
    public void setSpeedup(double speedup) { this.speedup = speedup; }
    public double getEfficiency() { return efficiency; }
    public void setEfficiency(double efficiency) { this.efficiency = efficiency; }
}