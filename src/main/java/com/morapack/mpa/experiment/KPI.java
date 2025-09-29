package com.morapack.mpa.experiment;

/** Contenedor de métricas para una corrida. */
public class KPI {
    public final long runtimeMs;
    public final double percentDelivered;

    public KPI(long runtimeMs, double percentDelivered) {
        this.runtimeMs = runtimeMs;
        this.percentDelivered = percentDelivered;
    }

    @Override public String toString() {
        return runtimeMs + "," + String.format("%.2f", percentDelivered);
    }
}
