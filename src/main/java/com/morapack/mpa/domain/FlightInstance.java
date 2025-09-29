package com.morapack.mpa.domain;

/**
 * Instancia temporal de un vuelo en la semana (ej. "BRU->LIM @ hora 14").
 * El tiempo se maneja en horas discretas [0..167] para el horizonte semanal.
 */
public class FlightInstance {
    private final Flight flight;
    private final int departureHour; // 0..167
    private final int arrivalHour;   // dep + dur (en horas)
    private int remainingCapacity;

    public FlightInstance(Flight flight, int departureHour, int arrivalHour) {
        this.flight = flight;
        this.departureHour = departureHour;
        this.arrivalHour = arrivalHour;
        this.remainingCapacity = flight.capacityPerInstance();
    }

    public Flight flight() { return flight; }
    public int departureHour() { return departureHour; }
    public int arrivalHour() { return arrivalHour; }
    public int remainingCapacity() { return remainingCapacity; }

    /** Reserva capacidad para N paquetes si hay cupo, devolviendo true en caso de éxito. */
    public boolean book(int units) {
        if (remainingCapacity >= units) {
            remainingCapacity -= units;
            return true;
        }
        return false;
    }
}
