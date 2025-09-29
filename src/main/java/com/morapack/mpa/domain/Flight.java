package com.morapack.mpa.domain;

import java.time.Duration;

/**
 * Definición abstracta de un vuelo entre dos aeropuertos.
 * La programación semanal se materializa en FlightInstance (uno por salida).
 */
public class Flight {
    private final String id;
    private final Airport origin;
    private final Airport destination;
    private final Duration duration;     // 12h intra, 24h inter (según caso)
    private final int capacityPerInstance;
    private final int frequencyPerDay;   // cantidad de salidas por día

    public Flight(String id, Airport origin, Airport destination, Duration duration, int capacityPerInstance, int frequencyPerDay) {
        this.id = id;
        this.origin = origin;
        this.destination = destination;
        this.duration = duration;
        this.capacityPerInstance = capacityPerInstance;
        this.frequencyPerDay = frequencyPerDay;
    }

    public String id() { return id; }
    public Airport origin() { return origin; }
    public Airport destination() { return destination; }
    public Duration duration() { return duration; }
    public int capacityPerInstance() { return capacityPerInstance; }
    public int frequencyPerDay() { return frequencyPerDay; }
}